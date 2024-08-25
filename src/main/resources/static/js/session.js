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
        return; // Exit if session ID is not available
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
            // Reload the page when a "reload" message is received
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
    };

    // Handle next question button click
    const nextQuestionButton = document.getElementById('nextQuestionButton');
    if (nextQuestionButton) {
        nextQuestionButton.addEventListener('click', function() {
            const sessionId = document.querySelector('input[name="sessionId"]').value;
            const currentIndex = document.getElementById('currentIndex') ? document.getElementById('currentIndex').value : '';

            console.log('Index: ', currentIndex);
            console.log('Session: ', sessionId);

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
                        throw new Error('Network response was not ok ' + response.statusText);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Success:', data);
                    socket.send('reload');
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        });
    } else {
        console.log('Next Question Button not found');
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
                        // Hide the end session button
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
                        alert('Failed to end session or no scores available.');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert(`An error occurred while ending the session: ${error.message}`);
                });
        });
    } else {
        console.log('End Session Button not found');
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
                        alert('You have left the session.');
                        window.location.href = "/explore"; // Redirect to home or appropriate page
                    } else {
                        alert('Failed to leave the session.');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert(`An error occurred while leaving the session: ${error.message}`);
                });
        });
    } else {
        console.log('Leave Session Button not found');
    }
});

// Define `selectVideo` function in the global scope
function selectVideo(optionId, mainBirdId, questionIndex, questionId) {
    const questionContainer = document.querySelector(`.question-container[data-question-index="${questionIndex}"]`);
    if (questionContainer) {
        const videoCard = questionContainer.querySelector(`.video-card[data-option-id="${optionId}"]`);
        const video = videoCard ? videoCard.querySelector('video') : null;

        if (video) {
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

// Define `sendAnswerToServer` function in the global scope
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
        credentials: 'include'  // This ensures cookies are sent with the request
    })
        .then(response => response.json())
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
                        feedback.textContent = data.message || "An error occurred.";
                    }
                } else {
                    console.error('Feedback element not found for question index:', questionIndex);
                }

                if (data.score !== undefined) {
                    console.log('Updated Score:', data.score);
                    // Update the score display on the page here
                }
            } else {
                console.error('Question container not found for questionIndex:', questionIndex);
            }
        })
        .catch(error => {
            console.error('Error sending answer:', error);
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
