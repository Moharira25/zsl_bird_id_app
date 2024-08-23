package com.zsl_birdid.services;

import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a new {@link UserService} with the specified {@link UserRepository}.
     *
     * @param userRepository The repository for handling {@link User} entities
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by their ID.
     *
     * @param userId The ID of the user to find
     * @return The {@link User} with the specified ID
     * @throws RuntimeException if the user is not found
     */
    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Resets the session state for a user.
     *
     * @param user The user whose session state is to be reset
     */
    public void resetUserSessionState(User user) {
        user.setCurrentSession(null);
        user.setInSession(false);
        user.setSessionScore(0);
        userRepository.save(user);
    }

    /**
     * Retrieves the user ID from a cookie in the HTTP request.
     *
     * @param request The HTTP request containing cookies
     * @return The user ID extracted from the cookie, or null if the cookie is not found
     */
    public UUID getUserIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("tempUserId".equals(cookie.getName())) {
                    System.out.println("Found cookie with value: " + cookie.getValue());
                    return UUID.fromString(cookie.getValue());
                }
            }
        }
        System.out.println("Cookie not found");
        return null;
    }
}
