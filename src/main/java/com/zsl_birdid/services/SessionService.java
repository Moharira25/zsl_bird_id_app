package com.zsl_birdid.services;

import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.domain.Question;
import com.zsl_birdid.Repo.SessionRepository;
import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.dto.SessionDTO;
import com.zsl_birdid.dto.SessionStats;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for handling business logic related to {@link Session} entities.
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@link SessionService} with the specified {@link SessionRepository},
     * {@link QuestionService}, and {@link UserRepository}.
     *
     * @param sessionRepository The repository for accessing {@link Session} entities
     * @param questionService   The service for handling {@link Question} entities
     * @param userRepository    The repository for accessing {@link User} entities
     */
    public SessionService(SessionRepository sessionRepository, QuestionService questionService, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.questionService = questionService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all sessions.
     *
     * @return A list of all {@link Session} entities
     */
    public List<Session> getAllSessions() {
        return (List<Session>) sessionRepository.findAll();
    }

    /**
     * Creates a new {@link Session} based on the provided {@link SessionDTO} and creator ID.
     *
     * @param sessionDTO The DTO containing session details
     * @param creatorId  The ID of the user creating the session
     * @return The created {@link Session}
     */
    public Session createSession(SessionDTO sessionDTO, UUID creatorId) {
        User creator = userRepository.findById(creatorId).orElseThrow(() -> new RuntimeException("User not found"));
        Session session = new Session();
        session.setName(sessionDTO.getName());
        session.setActive(true);
        session.setIndividual(Objects.equals(sessionDTO.getSessionType(), "individual"));
        session.setAdmin(creator);
        creator.setManagedSession(session);

        // Create and add 10 questions to the session
        for (int i = 0; i < 10; i++) {
            Question question = questionService.createQuestion();
            session.getQuestionList().add(question);
        }

        return sessionRepository.save(session);
    }

    /**
     * Ends the session with the specified ID and calculates the session statistics.
     *
     * @param id The ID of the session to be ended
     * @return True if the session was successfully ended, false otherwise
     */
    public boolean endSession(long id) {
        // Fetch the session from the repository
        Session session = sessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Session not found"));

        // Check if the session is active
        if (session.isActive()) {
            // Get the admin of the session
            User admin = session.getAdmin();

            // Calculate scores excluding the admin
            List<Integer> scores = session.getUserList().stream()
                    .filter(user -> !user.equals(admin)) // Exclude the admin
                    .map(User::getSessionScore) // Map to scores
                    .collect(Collectors.toList());

            if (!scores.isEmpty()) {
                // Calculate statistics
                int minScore = Collections.min(scores);
                int maxScore = Collections.max(scores);
                double averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                int medianScore = calculateMedian(scores);

                // Update session statistics
                session.setMinScore(minScore);
                session.setMaxScore(maxScore);
                session.setAverageScore((int) Math.round(averageScore));
                session.setMedianScore(medianScore);
            }

            // Deactivate session and clear admin
            session.setActive(false);
            session.setAdmin(null);

            // Save the session
            sessionRepository.save(session);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Calculates the median score from a list of scores.
     *
     * @param scores The list of scores
     * @return The median score
     */
    private int calculateMedian(List<Integer> scores) {
        Collections.sort(scores);
        int size = scores.size();
        if (size % 2 == 0) {
            return (scores.get(size / 2 - 1) + scores.get(size / 2)) / 2;
        } else {
            return scores.get(size / 2);
        }
    }

    /**
     * Adds a user to the specified session.
     *
     * @param sessionId The ID of the session
     * @param userId    The ID of the user to be added
     * @return True if the user was successfully added to the session, false otherwise
     */
    public boolean addUserToSession(long sessionId, UUID userId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!session.isActive()) {
            throw new RuntimeException("Session is not active");
        }



        boolean userInSession = session.getUserList().contains(user);
        if (!userInSession && !user.isInSession()) {
            session.getUserList().add(user);
            sessionRepository.save(session);
            user.setCurrentSession(session);
            user.setInSession(true);
            userRepository.save(user);
            return true;
        }

        return user.isInSession() && userInSession;
    }

    /**
     * Retrieves statistics for the specified session.
     *
     * @param sessionId The ID of the session
     * @return A {@link SessionStats} object containing the session statistics
     */
    public SessionStats getSessionStats(long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));

        int minScore = session.getMinScore();
        int maxScore = session.getMaxScore();
        int averageScore = session.getAverageScore();
        int medianScore = session.getMedianScore();

        return new SessionStats(minScore, maxScore, averageScore, medianScore, session.getUserList().size());
    }

    /**
     * Finds a session by its ID.
     *
     * @param sessionId The ID of the session
     * @return The {@link Session} with the specified ID
     */
    public Session findSessionById(long sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
    }
}
