// Declare baseUrl globally
let baseUrl;
let audioPlayed = {}; // To track if audio has been played

document.addEventListener('DOMContentLoaded', function() {
    // Get baseUrl from the current URL
    baseUrl = `${window.location.protocol}//${window.location.host}`;

    // Get sessionId from a data attribute or global variable
    const sessionId = document.querySelector('[data-session-id]')?.getAttribute('data-session-id') || window.sessionId;

    if (!sessionId) {
        console.error('Session ID is not available.');
        window.location.href = '/error_';
        return;
    }

    // Initialize WebSocket connection
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socket = new WebSocket(`${protocol}//${window.location.host}/ws/sessions/${sessionId}`);

    socket.onmessage = function(event) {
        const message = event.data;
        console.log("Received message: " + message);
        const messageContainer = document.getElementById("messageContainer");
        if (messageContainer) {
            const messageElement = document.createElement("p");
            messageElement.textContent = message;
            messageContainer.appendChild(messageElement);
        }

        if (message === "reload") {
            window.location.reload();
        }
    };

    socket.onopen = function() {
        console.log("WebSocket connection established");
    };

    socket.onclose = function() {
        console.log("WebSocket connection closed");
    };

    socket.onerror = function(error) {
        console.error("WebSocket error: ", error);
        window.location.href = '/error_';
    };

    // Handle next question button click
    const nextQuestionButton = document.getElementById('nextQuestionButton');
    if (nextQuestionButton) {
        nextQuestionButton.addEventListener('click', function() {
            const sessionId = this.getAttribute('data-session-id');
            const currentIndex = this.getAttribute('data-current-index');

            fetch(`${baseUrl}/api/questions/change`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    'sessionId': sessionId,
                    'currentIndex': currentIndex
                })
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Success:', data);
                    socket.send('reload');
                })
                .catch(error => {
                    console.error('Error:', error);
                    window.location.href = '/error_';
                });
        });
    }

    // Handle end session button click
    const endSessionButton = document.getElementById('endSessionButton');
    if (endSessionButton) {
        endSessionButton.addEventListener('click', function() {
            if (confirm('Are you sure you want to end the session?')) {
                const sessionId = this.getAttribute('data-session-id');

                fetch(`${baseUrl}/api/sessions/${sessionId}/end`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.success) {
                            endSessionButton.style.display = 'none';
                            const currentQuestion = document.querySelector('.question-container.question');
                            if (currentQuestion) {
                                currentQuestion.classList.add('hidden');
                            }
                            if (!data.isIndividual){
                                const statsContainer = document.getElementById('sessionStats');
                                if (statsContainer) {
                                    statsContainer.classList.remove('hidden');
                                    document.getElementById('minScore').innerText = `Minimum Score: ${data.minScore}`;
                                    document.getElementById('maxScore').innerText = `Maximum Score: ${data.maxScore}`;
                                    document.getElementById('averageScore').innerText = `Average Score: ${data.averageScore}`;
                                    document.getElementById('medianScore').innerText = `Median Score: ${data.medianScore}`;
                                    document.getElementById('numberOfParticipants').innerText = `Number of Participants: ${data.numberOfParticipants}`;
                                }
                            }
                        } else {
                            window.location.href = '/error_';
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        window.location.href = '/error_';
                    });
            }
        });
    }

    // Handle leave session button click
    const leaveSessionButton = document.getElementById('leaveSessionButton');
    if (leaveSessionButton) {
        leaveSessionButton.addEventListener('click', function() {
            if (confirm('Are you sure you want to leave the session?')) {
                const sessionId = this.getAttribute('data-session-id');

                fetch(`${baseUrl}/api/sessions/${sessionId}/leave`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ sessionId })
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.success) {
                            window.location.href = "/explore";
                        } else {
                            window.location.href = '/error_';
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        window.location.href = '/error_';
                    });
            }
        });
    }

    // Handle audio playback tracking
    const allAudio = document.querySelectorAll('audio');
    allAudio.forEach((audio) => {
        audio.addEventListener('play', () => {
            audioPlayed[audio.id] = true; // Track audio playback
        });
    });
});

function selectVideo(optionId, mainBirdId, questionIndex, questionId) {
    const questionContainer = document.querySelector(`.question-container[data-question-index="${questionIndex}"]`);
    if (questionContainer) {
        const videoCard = questionContainer.querySelector(`.video-card[data-option-id="${optionId}"]`);
        const video = videoCard ? videoCard.querySelector('video') : null;

        if (video) {
            const associatedAudioId = videoCard.getAttribute('data-audio-id');
            const associatedAudio = document.getElementById(associatedAudioId);

            if (associatedAudio && !audioPlayed[associatedAudioId]) {
                console.error('Audio must be played before this video.');
                alert('Please play the associated audio before playing the video.');
                return;
            }

            // Pause all other videos first
            const allVideos = document.querySelectorAll('video');
            allVideos.forEach((vid) => {
                if (vid !== video) {
                    vid.pause();
                }
            });

            // Play the selected video
            video.play();
        } else {
            console.error('Video element not found for optionId:', optionId);
        }

        sendAnswerToServer(optionId, mainBirdId, questionIndex, questionId);

        const feedback = questionContainer.querySelector('.feedback');
        if (feedback) {
            feedback.textContent = "Your attempt has been recorded.";
        } else {
            console.error('Feedback element not found for question index:', questionIndex);
        }
    } else {
        console.error('Question container not found for questionIndex:', questionIndex);
    }
}

function sendAnswerToServer(optionId, mainBirdId, questionIndex, questionId) {
    const url = new URL(`${baseUrl}/api/questions/answer`);
    url.searchParams.append('birdId', mainBirdId);
    url.searchParams.append('optionBirdId', optionId);
    url.searchParams.append('questionId', questionId);

    fetch(url.toString(), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            const questionContainer = document.querySelector(`.question-container[data-question-index="${questionIndex}"]`);
            if (questionContainer) {
                const videoCard = questionContainer.querySelector(`.video-card[data-option-id="${optionId}"]`);

                // Handling the 'Correct' and 'Incorrect' messages based on server response
                if (data.message === 'Correct') {
                    document.getElementById('userScore').innerText = `Score: ${data.score}`;
                    if (videoCard) {
                        videoCard.classList.add('correct');
                    }
                    console.log('Correct answer recorded.');
                } else if (data.message === 'Incorrect') {
                    if (videoCard) {
                        videoCard.classList.add('incorrect');
                    }
                    console.log('Incorrect answer recorded.');
                } else {
                    // Handle any other unexpected messages
                    console.error('Unexpected response message');
                }

                // Log updated score if available
                if (data.score !== undefined) {
                    console.log('Updated Score:', data.score);
                }
            }
        })
        .catch(error => {
            console.error('Error sending answer:', error);
            window.location.href = '/error_';
        });
}


function playAudio(index) {
    const audio = document.getElementById('audio-' + index);
    if (audio) {
        audio.play();
    } else {
        console.error('Audio element not found for index:', index);
    }
}
