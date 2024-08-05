package com.zsl_birdid.controller;

import com.zsl_birdid.domain.Session;
import com.zsl_birdid.services.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    @Autowired
    private SessionService sessionService;
    @GetMapping
    public List<Session> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @PostMapping
    public Session createSession(@RequestBody Session session) {
        return sessionService.createSession(session);
    }

    @PostMapping("/{sessionId}/join")
    public void joinSession(@PathVariable long sessionId, HttpServletRequest request) {
        // Retrieve the tempUserId from the cookies
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

        // If tempUserId is found, add user to the session
        if (tempUserId != null) {
            UUID userId = UUID.fromString(tempUserId);
            sessionService.addUserToSession(sessionId, userId);
        }
    }
}
