// Declare baseUrl globally
let baseUrl;

document.addEventListener('DOMContentLoaded', function() {
    // Get baseUrl from the current URL
    baseUrl = `${window.location.protocol}//${window.location.host}`;

    // Get sessionId from an element or global variable
    const sessionIdInput = document.querySelector('input[name="sessionId"]');
    const sessionId = sessionIdInput ? sessionIdInput.value : window.sessionId;

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
            const sessionId = document.querySelector('input[name="sessionId"]').value;
            const currentIndex = document.getElementById('currentIndex') ? document.getElementById('currentIndex').value : '';

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
            const sessionId = document.querySelector('input[name="sessionId"]').value || window.sessionId;

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
        });
    }

    // Handle leave session button click
    const leaveSessionButton = document.getElementById('leaveSessionButton');
    if (leaveSessionButton) {
        leaveSessionButton.addEventListener('click', function() {
            const sessionId = document.querySelector('input[name="sessionId"]').value || window.sessionId;

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
        });
    }
});

function selectVideo(optionId, mainBirdId, questionIndex, questionId) {
    const questionContainer = document.querySelector(`.question-container[data-question-index="${questionIndex}"]`);
    if (questionContainer) {
        const videoCard = questionContainer.querySelector(`.video-card[data-option-id="${optionId}"]`);
        const video = videoCard ? videoCard.querySelector('video') : null;

        if (video) {
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
                const feedback = questionContainer.querySelector('.feedback');
                const videoCard = questionContainer.querySelector(`.video-card[data-option-id="${optionId}"]`);

                if (feedback) {
                    if (data.message === 'Correct') {
                        feedback.textContent = "Correct answer!";
                        document.getElementById('userScore').innerText = `Score : ${data.score}`;
                        if (videoCard) videoCard.classList.add('correct');
                    } else if (data.message === 'Incorrect') {
                        feedback.textContent = "Incorrect answer.";
                        if (videoCard) videoCard.classList.add('incorrect');
                    } else if (data.message === 'Question already answered.') {
                        feedback.textContent = "You've already answered this question.";
                    } else {
                        feedback.textContent = "An error occurred.";
                    }
                }

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