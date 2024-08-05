package com.zsl_birdid.controller;

import com.zsl_birdid.Repo.UserRepository;
import com.zsl_birdid.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/explore")
    public String exploreSessions(HttpServletRequest request, HttpServletResponse response) {
        // Check if user already has a temp ID cookie
        Cookie[] cookies = request.getCookies();
        boolean hasTempId = false;
        String tempUserId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("tempUserId".equals(cookie.getName())) {
                    hasTempId = true;
                    tempUserId = cookie.getValue();
                    break;
                }
            }
        }

        // If no temp ID cookie, generate a new user, save to DB, and add a new cookie
        if (!hasTempId) {
            User newUser = new User();
            userRepository.save(newUser);
            tempUserId = newUser.getId().toString();
            Cookie tempIdCookie = new Cookie("tempUserId", tempUserId);
            tempIdCookie.setPath("/");
            tempIdCookie.setHttpOnly(true); // Secure the cookie from JavaScript access
            tempIdCookie.setMaxAge(24 * 60 * 60); // Set cookie to expire in 24 hours
            response.addCookie(tempIdCookie);
        }

        return "sessions";
    }


}
