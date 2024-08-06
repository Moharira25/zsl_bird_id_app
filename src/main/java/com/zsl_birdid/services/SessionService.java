package com.zsl_birdid.services;

import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.Repo.SessionRepository;
import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.websocket.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MyWebSocketHandler myWebSocketHandler;

    // Method to get all sessions
    public List<Session> getAllSessions() {
        return (List<Session>) sessionRepository.findAll();
    }

    public Session createSession(Session session, User creator) {
        // Ensure that the session has a user list
        if (session.getUserList() == null) {
            session.setUserList(new ArrayList<>());
        }

        // Set the creator as the admin of the session
        if (creator != null) {
            session.setAdmin(creator);
            creator.setRole("ADMIN");
            creator.setManagedSession(session);
            userRepository.save(creator); // Save the updated user with admin role

        }

        // Save and return the new session
        session.setActive(true);
        return sessionRepository.save(session);
    }


    // Method to handle user joining session
    public boolean addUserToSession(long sessionId, UUID userId) {
        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (sessionOptional.isPresent() && userOptional.isPresent()) {
            Session session = sessionOptional.get();
            User user = userOptional.get();

            if (session.isActive()) {
                if (!session.getUserList().contains(user)) {
                    session.getUserList().add(user);
                    sessionRepository.save(session);

                    myWebSocketHandler.sendMessageToSession(sessionId, "User " + user.getId() + " joined session!");
                    return true;
                }
            } else {
                throw new RuntimeException("Session is not active.");
            }
        } else {
            throw new RuntimeException("Session or User not found.");
        }
        return false;
    }

    // Method to extract user ID from cookies
    public UUID getUserIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String tempUserId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("tempUserId".equals(cookie.getName())) {
                    tempUserId = cookie.getValue();
                    break;
                }
            }
        }
        if (tempUserId != null) {
            return UUID.fromString(tempUserId);
        } else {
            return null;
        }
    }
}
