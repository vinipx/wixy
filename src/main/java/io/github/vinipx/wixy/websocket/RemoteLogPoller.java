package io.github.vinipx.wixy.websocket;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.WireMockEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class RemoteLogPoller {

    private static final Logger log = LoggerFactory.getLogger(RemoteLogPoller.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final LogWebSocketHandler logWebSocketHandler;
    private final EngineManager engineManager;
    private final Map<String, Set<String>> activeEngines = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<String>> lastServeEventIdByEngine = new ConcurrentHashMap<>();

    public RemoteLogPoller(LogWebSocketHandler logWebSocketHandler, EngineManager engineManager) {
        this.logWebSocketHandler = logWebSocketHandler;
        this.engineManager = engineManager;
    }

    public void registerInterest(String engineId) {
        if ("local".equals(engineId)) return;
        activeEngines.computeIfAbsent(engineId, k -> ConcurrentHashMap.newKeySet()).add("subscriber");
    }

    public void unregisterInterest(String engineId) {
        Set<String> subscribers = activeEngines.get(engineId);
        if (subscribers != null) {
            subscribers.remove("subscriber");
            if (subscribers.isEmpty()) {
                activeEngines.remove(engineId);
            }
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void pollLogs() {
        if (activeEngines.isEmpty()) {
            return;
        }
        log.debug("Polling logs for active engines: {}", activeEngines.keySet());
        for (String engineId : activeEngines.keySet()) {
            try {
                WireMockEngine activeEngine = engineManager.getActiveEngine();
                String activeId = engineManager.getActiveServerId() != null ? engineManager.getActiveServerId().toString() : "local";
                
                log.debug("EngineId: {}, ActiveId: {}", engineId, activeId);
                if (engineId.equals(activeId) && !"local".equals(engineId)) {
                    log.debug("Executing poll for engine {}", engineId);
                    pollEngine(engineId, activeEngine);
                }
            } catch (Exception e) {
                log.error("Failed to poll logs for engine {}", engineId, e);
            }
        }
    }

    private void pollEngine(String engineId, WireMockEngine engine) {
        try {
            List<ServeEvent> events = engine.listServeEvents();
            log.debug("Found {} events for engine {}", events != null ? events.size() : 0, engineId);
            if (events == null || events.isEmpty()) return;

            AtomicReference<String> lastIdRef = lastServeEventIdByEngine.computeIfAbsent(engineId, k -> new AtomicReference<>());
            String lastId = lastIdRef.get();
            
            // WireMock events are usually returned newest first.
            // We want to process only new events.
            List<ServeEvent> newEvents = events.stream()
                    .takeWhile(e -> !e.getId().toString().equals(lastId))
                    .collect(Collectors.toList());
            
            Collections.reverse(newEvents); // Now oldest to newest

            for (ServeEvent event : newEvents) {
                String method = event.getRequest().getMethod().getName();
                String url = event.getRequest().getUrl();
                int status = event.getResponse().getStatus();
                
                String time = LocalDateTime.now().format(TIME_FORMATTER);
                String prefix = "\u001B[90m[" + time + "]\u001B[0m \u001B[33m[REMOTE]\u001B[0m ";
                
                String logLine = String.format("%s %s -> %s", 
                        getMethodColor(method) + method + "\u001B[0m",
                        "\u001B[36m" + url + "\u001B[0m",
                        getStatusColor(status) + status + "\u001B[0m");
                
                logWebSocketHandler.broadcastLog(engineId, prefix + logLine);
            }

            if (!events.isEmpty()) {
                lastIdRef.set(events.get(0).getId().toString());
            }
        } catch (Exception e) {
            log.error("Error polling engine {}: {}", engineId, e.getMessage());
        }
    }

    private String getMethodColor(String method) {
        return switch (method) {
            case "GET" -> "\u001B[34m"; // Blue
            case "POST" -> "\u001B[32m"; // Green
            case "PUT" -> "\u001B[33m"; // Yellow
            case "DELETE" -> "\u001B[31m"; // Red
            default -> "\u001B[35m"; // Magenta
        };
    }

    private String getStatusColor(int status) {
        if (status >= 200 && status < 300) return "\u001B[32m"; // Green
        if (status >= 300 && status < 400) return "\u001B[34m"; // Blue
        if (status >= 400 && status < 500) return "\u001B[33m"; // Yellow
        if (status >= 500) return "\u001B[31m"; // Red
        return "\u001B[37m"; // White
    }
}
