package com.zsl_birdid.controller;

import com.zsl_birdid.Repo.BirdRepository;
import com.zsl_birdid.Repo.SessionRepository;
import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.Bird;
import com.zsl_birdid.domain.Session;
import com.zsl_birdid.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private BirdRepository birdRepository;


    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/bird")
    public String birdData(Model model) {
        List<Bird> birds = (List<Bird>) birdRepository.findAll();
        model.addAttribute("birds", birds);
        return "test";
    }

    @RequestMapping("/session/{id}")
    public String session(@PathVariable long id, Model model) {
        Session session = sessionRepository.findById(id).orElse(null);
        if (session == null){
            model.addAttribute("sessionId", null);

        }else {
            model.addAttribute("sessionId", id);
        }

        return "session";
    }

    @RequestMapping("/explore")
    public String exploreSessions(HttpServletRequest request, HttpServletResponse response, Model model) {
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

        try {
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
                tempUserId = user.getId().toString();

                // Set new cookie with the new user's ID
                Cookie tempIdCookie = new Cookie("tempUserId", tempUserId);
                tempIdCookie.setPath("/");
                tempIdCookie.setHttpOnly(true);
                tempIdCookie.setSecure(true); // Only set this if using HTTPS
                tempIdCookie.setMaxAge(24 * 60 * 60); // 24 hours
                response.addCookie(tempIdCookie);
            }

            List<Session> sessions = (List<Session>) sessionRepository.findAll();
            model.addAttribute("sessions", sessions);
            model.addAttribute("user", user); // Optionally add user to the model if needed in view
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            // Add an error message to the model
            model.addAttribute("error", "An error occurred while processing your request.");
        }

        return "sessions";
    }


}
