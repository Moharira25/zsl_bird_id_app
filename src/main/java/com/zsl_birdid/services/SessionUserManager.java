package com.zsl_birdid.services;

import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing user actions related to session participation and management.
 */
@Service
public class SessionUserManager {

    private final SessionService sessionService;
    private final UserService userService;

    /**
     * Constructs a new {@link SessionUserManager} with the specified {@link SessionService}
     * and {@link UserService}.
     *
     * @param sessionService The service for handling {@link Session} entities
     * @param userService    The service for handling {@link User} entities
     */
    @Autowired
    public SessionUserManager(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    /**
     * Handles the logic for a user leaving a session.
     *
     * @param userId    The ID of the user leaving the session
     * @param sessionId The ID of the session from which the user is leaving
     */
    public void handleUserLeavingSession(UUID userId, long sessionId) {
        User user = userService.findById(userId);
        // User is a participant, handle participant session leave logic
        handleParticipantSessionLeave(user, sessionId);
        if (user.getManagedSession() != null) {
            // User is an admin, handle admin session leave logic
            handleManagedSessionLeave(user);
        }

        // Reset user's session state regardless of their role
        userService.resetUserSessionState(user);
    }

    /**
     * Handles the logic when an admin leaves their managed session.
     *
     * @param user The admin user leaving the session
     */
    private void handleManagedSessionLeave(User user) {
        Session session = user.getManagedSession();
        user.setManagedSession(null);

        if (session.isIndividual()) {
            // For individual sessions, end the session and remove the user
            sessionService.endSession(session.getId());
            session.getUserList().remove(user);
            user.setCurrentSession(null);
        } else {
            // For non-individual sessions, reassign admin or end session if no users left
            if (!session.getUserList().isEmpty()) {
                session.setAdmin(session.getUserList().get(0));
            } else {
                sessionService.endSession(session.getId());
            }
        }
    }

    /**
     * Handles the logic when a participant leaves a session.
     *
     * @param user      The participant user leaving the session
     * @param sessionId The ID of the session from which the user is leaving
     */
    private void handleParticipantSessionLeave(User user, long sessionId) {
        Session session = sessionService.findSessionById(sessionId);
        session.getUserList().remove(user);
    }
}
