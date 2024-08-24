// Declare baseUrl globally
let baseUrl;

document.addEventListener('DOMContentLoaded', function() {
    // Set baseUrl from the current URL
    baseUrl = `${window.location.protocol}//${window.location.host}`;

    // Function to create a session
    function createSession(event) {
        event.preventDefault(); // Prevent form submission

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
                    // Handle failed session creation
                    return response.text().then(text => {
                        console.error("Failed to create session:", text);
                    });
                }
            })
            .catch(error => {
                console.error("Network error:", error); // Log network error
            });
    }

    // Attach the createSession function to the form's submit event
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', createSession);
    }
});
