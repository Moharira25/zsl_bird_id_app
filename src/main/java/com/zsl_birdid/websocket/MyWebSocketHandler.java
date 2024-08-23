package com.zsl_birdid.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * WebSocket handler for managing connections and messaging within different sessions.
 */
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    // Map to keep track of WebSocket sessions per session ID
    private final Map<Long, Set<WebSocketSession>> sessionMap = new HashMap<>();

    /**
     * Called after a WebSocket connection is established.
     *
     * @param session The WebSocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        long sessionId = extractSessionIdFromUri(Objects.requireNonNull(session.getUri()).toString());
        registerSession(sessionId, session);
        System.out.println("Connection established for session: " + sessionId);
    }

    /**
     * Called after a WebSocket connection is closed.
     *
     * @param session The WebSocket session
     * @param status  The close status
     * @throws Exception If an error occurs
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        long sessionId = extractSessionIdFromUri(Objects.requireNonNull(session.getUri()).toString());
        unregisterSession(sessionId, session);
        System.out.println("Connection closed for session: " + sessionId);
    }

    /**
     * Registers a WebSocket session for a given session ID.
     *
     * @param sessionId The session ID
     * @param session   The WebSocket session to register
     */
    public void registerSession(long sessionId, WebSocketSession session) {
        sessionMap.computeIfAbsent(sessionId, k -> new HashSet<>()).add(session);
    }

    /**
     * Unregisters a WebSocket session for a given session ID.
     *
     * @param sessionId The session ID
     * @param session   The WebSocket session to unregister
     */
    public void unregisterSession(long sessionId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionMap.get(sessionId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionMap.remove(sessionId);
            }
        }
    }

    /**
     * Extracts the session ID from the WebSocket URI.
     *
     * @param uri The WebSocket URI
     * @return The extracted session ID
     */
    private long extractSessionIdFromUri(String uri) {
        // Assuming URI format: ws://localhost:8080/ws/sessions/{sessionId}
        String[] parts = uri.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }

    /**
     * Handles incoming text messages.
     *
     * @param session The WebSocket session
     * @param message The text message
     * @throws Exception If an error occurs
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        long sessionId = extractSessionIdFromUri(Objects.requireNonNull(session.getUri()).toString());
        sendMessageToSession(sessionId, message.getPayload());
    }

    /**
     * Sends a message to all WebSocket sessions associated with a given session ID.
     *
     * @param sessionId The session ID
     * @param message   The message to send
     */
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
