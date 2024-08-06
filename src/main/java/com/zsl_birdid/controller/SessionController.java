package com.zsl_birdid.controller;

import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.services.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createSession(@RequestBody Session session, HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromCookies(request);

        if (userId != null) {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                sessionService.createSession(session, user);
                return ResponseEntity.status(HttpStatus.CREATED).body("Session created successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Temp user ID not found in cookies");
        }
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<String> joinSession(@PathVariable long sessionId, HttpServletRequest request) {
        UUID userId = sessionService.getUserIdFromCookies(request);

        if (userId != null) {
            boolean userAdded = sessionService.addUserToSession(sessionId, userId);

            if (userAdded) {
                return ResponseEntity.status(HttpStatus.OK).body("Joined session successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or user could not be added");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Temp user ID not found in cookies");
        }
    }
}