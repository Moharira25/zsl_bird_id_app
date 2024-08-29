package com.zsl_birdid.controller;

import com.zsl_birdid.Repo.BirdRepository;
import com.zsl_birdid.Repo.SessionRepository;
import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Bird;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.services.UserService;
import com.zsl_birdid.websocket.MyWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * Controller for handling API requests related to questions in sessions.
 */
@RestController
@RequestMapping("/api/questions")  // Prefix all endpoints with /api/questions
public class QuestionController {

    private final UserRepository userRepository;
    private final BirdRepository birdRepository;
    private final SessionRepository sessionRepository;
    private final MyWebSocketHandler myWebSocketHandler;
    private final UserService userService;

    /**
     * Constructor for QuestionController.
     * Initializes repositories and services.
     *
     * @param userRepository       Repository for User entities
     * @param birdRepository       Repository for Bird entities
     * @param sessionRepository    Repository for Session entities
     * @param myWebSocketHandler   WebSocket handler for real-time communication
     * @param userService          Service for managing users
     */
    public QuestionController(UserRepository userRepository, BirdRepository birdRepository, SessionRepository sessionRepository, MyWebSocketHandler myWebSocketHandler, UserService userService) {
        this.userRepository = userRepository;
        this.birdRepository = birdRepository;
        this.sessionRepository = sessionRepository;
        this.myWebSocketHandler = myWebSocketHandler;
        this.userService = userService;
    }

    /**
     * Changes the current question index for a session.
     * Only accessible by the user managing the session.
     *
     * @param sessionId    ID of the session
     * @param currentIndex Current question index
     * @param request      HTTP request containing user information
     * @return ResponseEntity with status and message
     */
    @PostMapping("/change")
    public ResponseEntity<Map<String, Object>> changeCurrentQuestion(@RequestParam long sessionId,
                                                                     @RequestParam int currentIndex,
                                                                     HttpServletRequest request) {
        UUID userId = userService.getUserIdFromRequest(request);

        // Find the session by ID
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (optionalSession.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Session not found"));
        }

        Session session = optionalSession.get();

        // Find the user by ID
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        User user = optionalUser.get();

        // Check if the user manages the session
        if (!user.getManagedSession().equals(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User does not manage this session"));
        }

        // Ensure there are more questions to display
        if (currentIndex + 1 >= session.getQuestionList().size()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No more questions available"));
        }

        // Update the session's question index
        session.setQuestionIndex(currentIndex + 1);
        // Notify all clients about the change via WebSocket
        myWebSocketHandler.sendMessageToSession(sessionId, "reload");
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "message", "Current question changed to index: " + session.getQuestionIndex(),
                "newIndex", session.getQuestionIndex()
        ));
    }

    /**
     * Checks the user's answer to a question.
     *
     * @param request      HTTP request containing user information
     * @param birdId       ID of the main bird
     * @param optionBirdId ID of the option bird selected by the user
     * @param questionId   ID of the question being answered
     * @return ResponseEntity with status and result of the check
     */
    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> checkAnswer(HttpServletRequest request,
                                                           @RequestParam long birdId,
                                                           @RequestParam long optionBirdId,
                                                           @RequestParam long questionId) {
        UUID userId = userService.getUserIdFromRequest(request);
        Bird mainBird = birdRepository.findById(birdId).orElse(null);
        Bird optionBird = birdRepository.findById(optionBirdId).orElse(null);

        Map<String, Object> response = new HashMap<>();
        HttpStatus status = HttpStatus.OK;

        if (userId == null) {
            response.put("message", "User not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && mainBird != null && optionBird != null) {
            boolean correct = Objects.equals(mainBird.getBirdName(), optionBird.getBirdName());

            // Mark the question as answered regardless of correctness
            user.getAnsweredQuestions().add(questionId);

            if (correct) {
                if (!hasUserAlreadyAnswered(user, questionId)) {
                    //update the score if the user hasn't answered the question before
                    user.setSessionScore(user.getSessionScore() + 1);
                }
                response.put("message", "Correct");
            } else {
                response.put("message", "Incorrect");
            }

            userRepository.save(user); // Save updated user
            response.put("score", user.getSessionScore());
        } else {
            response.put("message", "Something went wrong.");
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(response, status);
    }

    /**
     * Checks if the user has already answered a specific question.
     *
     * @param user        User entity
     * @param questionId  ID of the question
     * @return true if the user has already answered the question, false otherwise
     */
    private boolean hasUserAlreadyAnswered(User user, long questionId) {
        return user.getAnsweredQuestions().contains(questionId);
    }
}
