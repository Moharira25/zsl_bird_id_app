package com.zsl_birdid.controller;

import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.dto.SessionDTO;
import com.zsl_birdid.dto.SessionStats;
import com.zsl_birdid.services.SessionService;
import com.zsl_birdid.services.SessionUserManager;
import com.zsl_birdid.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

/**
 * Controller for handling API requests related to sessions.
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SessionUserManager sessionUserManager;

    /**
     * Constructor for SessionController.
     * Initializes the required services and repositories.
     *
     * @param sessionService         Service for managing sessions
     * @param userRepository         Repository for User entities
     * @param userService            Service for managing users
     * @param sessionUserManager     Service for managing users in sessions
     */
    public SessionController(SessionService sessionService, UserRepository userRepository, UserService userService, SessionUserManager sessionUserManager) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionUserManager = sessionUserManager;
    }

    /**
     * Creates a new session.
     *
     * @param session   DTO containing session details
     * @param request   HTTP request containing user information
     * @return ResponseEntity with status and created session details
     */
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody SessionDTO session, HttpServletRequest request) {
        UUID userId = userService.getUserIdFromRequest(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "User ID not found in request"));
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Session createdSession = sessionService.createSession(session, user.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "User not found"));
        }
    }

    /**
     * Joins a session.
     *
     * @param sessionId   ID of the session to join
     * @param request     HTTP request containing user information
     * @return ResponseEntity with status and redirect location if successful
     */
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<?> joinSession(@PathVariable long sessionId, HttpServletRequest request) {
        UUID userId = userService.getUserIdFromRequest(request);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID not found in request");
        }

        try {
            boolean userAdded = sessionService.addUserToSession(sessionId, userId);

            if (userAdded) {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create("/session/" + sessionId));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Something Went Wrong");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Ends a session and retrieves statistics.
     *
     * @param sessionId   ID of the session to end
     * @return ResponseEntity with session statistics or error message
     */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endSession(@PathVariable long sessionId) {
        try {
            // Log session ID for debugging purposes
            System.out.println("Attempting to end session with ID: " + sessionId);

            //getting the session from the repo
            Session session = sessionService.findSessionById(sessionId);

            boolean isEnded = sessionService.endSession(sessionId);

            if (isEnded) {
                // Retrieve session statistics
                SessionStats stats = sessionService.getSessionStats(sessionId);

                // Create a response object with statistics
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("minScore", stats.getMinScore());
                response.put("maxScore", stats.getMaxScore());
                response.put("averageScore", stats.getAverageScore());
                response.put("medianScore", stats.getMedianScore());
                response.put("numberOfParticipants", stats.getNumberOfParticipants());
                response.put("isIndividual", session.isIndividual());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Session is already inactive."));
            }
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error while ending session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An error occurred while ending the session."));
        }
    }

    /**
     * Handles a user leaving a session.
     *
     * @param request     HTTP request containing user information
     * @param sessionId   ID of the session to leave
     * @return ResponseEntity with success message or error
     */
    @PostMapping("/{sessionId}/leave")
    public ResponseEntity<?> leaveSession(HttpServletRequest request, @PathVariable long sessionId) {
        try {
            UUID userId = userService.getUserIdFromRequest(request);
            sessionUserManager.handleUserLeavingSession(userId, sessionId);
            return ResponseEntity.ok(Collections.singletonMap("success", "User has left the session successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
