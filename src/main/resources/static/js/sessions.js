// Declare baseUrl globally
let baseUrl;

document.addEventListener('DOMContentLoaded', function() {
    // Set baseUrl from the current URL
    baseUrl = `${window.location.protocol}//${window.location.host}`;

    // Function to create a session
    function createSession(event) {
        event.preventDefault(); // Prevent form submission

        // Read hidden inputs
        const userInSession = document.getElementById('userInSession').value === 'true';

        if (userInSession) {
            const userInSessionActive = document.getElementById('userInSessionActive').value === 'true';
            if (userInSessionActive) {
                if (!confirm("You are already in an active session. Creating a new session will mean leaving the current one. Do you want to proceed?")) {
                    return; // User decided not to proceed
                }
            }
            // Automatically leave the current session if it is inactive or user confirmed to leave
            leaveCurrentSession().then(() => {
                proceedWithSessionCreation(event);
            }).catch(() => {
                window.location.href = '/error_';
            });
        } else {
            proceedWithSessionCreation(event);
        }
    }

    // Function to leave the current session
    function leaveCurrentSession() {
        const sessionId = document.getElementById('currentSessionId').value;
        return fetch(`${baseUrl}/api/sessions/${sessionId}/leave`, {
            method: 'POST',
            credentials: 'include', // Ensures cookies are sent
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ sessionId })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to leave session');
                }
                console.log("Successfully left the session.");
            })
            .catch(error => {
                console.error("Error leaving session:", error.message);
                throw error; // Re-throw the error to be caught in the calling function
            });
    }

    // Function to proceed with creating a session
    function proceedWithSessionCreation(event) {
        // Collect form data
        const formData = new FormData(event.target);
        const data = {
            name: formData.get('name'),  // Get session name
            sessionType: formData.get('type')  // Get session type
        };

        console.log('Creating session with data:', data);

        // Send a POST request to create a session
        fetch(`${baseUrl}/api/sessions`, {
            method: "POST",
            credentials: 'include', // Ensures cookies are sent
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data) // Convert data to JSON format
        })
            .then(response => {
                if (response.ok) {
                    console.log("Session created successfully");
                    window.location.href = "/explore"; // Redirect to the sessions page or another page
                } else {
                    throw new Error('Failed to create session');
                }
            })
            .catch(error => {
                console.error("Error creating session:", error.message);
                window.location.href = '/error_'; // Redirect to error page
            });
    }

    // Attach the createSession function to the form's submit event
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', createSession);
    }
});