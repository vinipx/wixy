package io.github.vinipx.wixy.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Lazy;

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LogWebSocketHandler.class);
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> sessionsByEngineId = new ConcurrentHashMap<>();
    private final RemoteLogPoller remoteLogPoller;

    public LogWebSocketHandler(@Lazy RemoteLogPoller remoteLogPoller) {
        this.remoteLogPoller = remoteLogPoller;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String engineId = getEngineId(session);
        log.info("WebSocket session attempting establishment for engine {}: {}", engineId, session.getId());
        sessionsByEngineId.computeIfAbsent(engineId, k -> new CopyOnWriteArrayList<>()).add(session);
        remoteLogPoller.registerInterest(engineId);
        log.info("WebSocket session established for engine {}: {}", engineId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String engineId = getEngineId(session);
        CopyOnWriteArrayList<WebSocketSession> sessions = sessionsByEngineId.get(engineId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                remoteLogPoller.unregisterInterest(engineId);
            }
        }
        log.info("WebSocket session closed for engine {}: {}", engineId, session.getId());
    }

    private String getEngineId(WebSocketSession session) {
        String path = session.getUri().getPath();
        log.debug("WebSocket path: {}", path);
        String[] parts = path.split("/");
        String engineId = parts[parts.length - 1]; // /api/ws/logs/{engineId}
        log.debug("Extracted engineId: {}", engineId);
        return engineId;
    }

    public void broadcastLog(String engineId, String message) {
        CopyOnWriteArrayList<WebSocketSession> sessions = sessionsByEngineId.get(engineId);
        // Also broadcast to "local" if it's the default engine
        if ("local".equals(engineId) || engineId == null) {
             sessions = sessionsByEngineId.getOrDefault("local", new CopyOnWriteArrayList<>());
             CopyOnWriteArrayList<WebSocketSession> defaultSessions = sessionsByEngineId.get("default");
             if(defaultSessions != null) {
                 sessions.addAll(defaultSessions);
             }
        }
        
        if (sessions != null && !sessions.isEmpty()) {
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send log message to session {}", session.getId(), e);
                    }
                }
            }
        }
    }
}
