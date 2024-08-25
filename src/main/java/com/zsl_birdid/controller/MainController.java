package com.zsl_birdid.controller;



import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import com.zsl_birdid.services.SessionService;
import com.zsl_birdid.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class MainController {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final UserService userService;

    /**
     * Constructor for MainController.
     * Initializes repositories and services.
     *
     * @param userRepository   Repository for User entities
     * @param sessionService   Service for managing sessions
     * @param userService      Service for managing users
     */
    public MainController(UserRepository userRepository, SessionService sessionService, UserService userService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    /**
     * Handles requests to the root URL ("/").
     * Returns the view name for the homepage.
     *
     * @return the name of the view to render
     */
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Handles requests to the error page.
     * This endpoint is designed to be called when an error occurs in the application.
     * It returns the name of the error page view, which should be resolved to an
     * appropriate error page template by Spring's view resolver.
     *
     * @return String The name of the error page view ("errorPage")
     */
    @GetMapping("/error_")
    public String handleError() {
        return "error";
    }

    /**
     * Handles requests to "/session/{id}".
     * Retrieves a Session entity by ID and checks if the current user is allowed to access it.
     *
     * @param id     the ID of the session
     * @param model  the model to add attributes to
     * @param request the HTTP request containing user information
     * @return the name of the view to render
     */
    @RequestMapping("/session/{id}")
    public String session(@PathVariable long id, Model model, HttpServletRequest request) {
        try {
            UUID userId = userService.getUserIdFromRequest(request);
            Session session = sessionService.findSessionById(id);

            if (userId != null) {
                User user = userService.findById(userId);
                model.addAttribute("user", user);
            }

            if (!sessionService.addUserToSession(id, userId)) {
                // User must join the session first; access denied if not a member
                return "sessions";
            }

            model.addAttribute("sessionId", id);
            model.addAttribute("session_", session);
            return "session";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while processing your request.");
            return "error";
        }
    }


    /**
     * Handles requests to "/explore".
     * Retrieves and manages user information based on cookies and displays all sessions.
     *
     * @param request  the HTTP request containing cookies
     * @param response the HTTP response to set cookies
     * @param model    the model to add attributes to
     * @return the name of the view to render
     */
    @RequestMapping("/explore")
    public String exploreSessions(HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            String tempUserId = null;
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("tempUserId".equals(cookie.getName())) {
                        tempUserId = cookie.getValue();
                        break;
                    }
                }
            }

            User user = null;
            if (tempUserId != null) {
                // Try to find the user in the database
                Optional<User> optionalUser = userRepository.findById(UUID.fromString(tempUserId));
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                } else {
                    // If user not found, delete the invalid cookie
                    Cookie deleteCookie = new Cookie("tempUserId", null);
                    deleteCookie.setPath("/");
                    deleteCookie.setMaxAge(0);
                    response.addCookie(deleteCookie);
                    tempUserId = null; // Reset tempUserId to trigger new user creation
                }
            }

            if (tempUserId == null) {
                // Create a new user
                user = new User();
                userRepository.save(user);

                Cookie cookie = new Cookie("tempUserId", user.getId().toString());
                cookie.setHttpOnly(true); // Prevents JavaScript access
                cookie.setPath("/"); // Available throughout the domain
                cookie.setMaxAge(60 * 60 * 24); // 1 day
                response.addCookie(cookie);
            }

            List<Session> sessions = sessionService.getAllSessions();
            model.addAttribute("sessions", sessions);
            model.addAttribute("user", user); // Optionally add user to the model if needed in view
            return "sessions";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while processing your request.");
            return "error";
        }
    }
}
