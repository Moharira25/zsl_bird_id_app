package com.zsl_birdid.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> sessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Extract session ID from the URI or from a handshake attribute
        String uri = session.getUri().toString();
        long sessionId = extractSessionIdFromUri(uri);
        registerSession(sessionId, session);
        System.out.println("Connection established for session: " + sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Extract session ID from the WebSocket session or URI
        String uri = session.getUri().toString();
        long sessionId = extractSessionIdFromUri(uri);
        unregisterSession(sessionId, session);
        System.out.println("Connection closed for session: " + sessionId);
    }

    public void registerSession(long sessionId, WebSocketSession session) {
        sessionMap.computeIfAbsent(sessionId, k -> new HashSet<>()).add(session);
    }

    public void unregisterSession(long sessionId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionMap.get(sessionId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(sessionId);
            }
        }
    }

    private long extractSessionIdFromUri(String uri) {
        // Parse the URI to get the session ID. This depends on your URL structure.
        // Example: ws://localhost:8080/ws/sessions/{sessionId}
        String[] parts = uri.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Extract session ID from the WebSocket session or URI
        String uri = session.getUri().toString();
        long sessionId = extractSessionIdFromUri(uri);
        sendMessageToSession(sessionId, message.getPayload());
    }

    public void sendMessageToSession(long sessionId, String message) {
        Set<WebSocketSession> sessions = sessionMap.get(sessionId);
        if (sessions != null) {
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen()) {
                    try {
                        System.out.println("Sending message to session: " + sessionId + ", message: " + message);
                        webSocketSession.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("No sessions found for sessionId: " + sessionId);
        }
    }
}
