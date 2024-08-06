package com.zsl_birdid.controller;

import com.zsl_birdid.websocket.MyWebSocketHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {



    // Get the current question
    @GetMapping("/current")
    public String getCurrentQuestion() {
        // Logic to get the current question
        return "Current question";
    }

    // Change the current question (admin only)
    @PostMapping("/change")
    public String changeCurrentQuestion(@RequestBody String newQuestion) {
        // Logic to change the current question
        //webSocketHandler.sendMessageToAll(newQuestion);
        return "Current question changed to: " + newQuestion;
    }
}
