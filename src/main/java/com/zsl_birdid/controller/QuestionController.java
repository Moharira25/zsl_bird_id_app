package com.zsl_birdid.controller;


import com.zsl_birdid.websocket.MyWebSocketHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private MyWebSocketHandler webSocketHandler;

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
        for (WebSocketSession session : webSocketHandler.getSessions()) {
            try {
                session.sendMessage(new TextMessage(newQuestion));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Current question changed to: " + newQuestion;
    }
}
