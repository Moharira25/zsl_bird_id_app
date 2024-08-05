package com.zsl_birdid.services;

import com.zsl_birdid.Repo.SessionRepository;
import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Session> getAllSessions() {
        return (List<Session>) sessionRepository.findAll();
    }

    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }



    public void addUserToSession(long sessionId, UUID userId) {
        Optional<Session> sessionOptional = sessionRepository.findById(sessionId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (sessionOptional.isPresent() && userOptional.isPresent()) {
            Session session = sessionOptional.get();
            User user = userOptional.get();

            // Check if the session is active
            if (session.isActive()) {
                // Add user to session if not already present
                if (!session.getUserList().contains(user)) {
                    session.getUserList().add(user);
                    sessionRepository.save(session);
                }
            } else {
                throw new RuntimeException("Session is not active.");
            }
        } else {
            throw new RuntimeException("Session or User not found.");
        }
    }
}
