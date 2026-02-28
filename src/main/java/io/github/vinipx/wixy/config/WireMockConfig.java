package io.github.vinipx.wixy.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import io.github.vinipx.wixy.websocket.LogWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates and manages the lifecycle of the embedded {@link WireMockServer}.
 */
@Configuration
public class WireMockConfig {

    private static final Logger log = LoggerFactory.getLogger(WireMockConfig.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final WixyProperties properties;
    private final LogWebSocketHandler logWebSocketHandler;
    private WireMockServer wireMockServer;

    public WireMockConfig(WixyProperties properties, LogWebSocketHandler logWebSocketHandler) {
        this.properties = properties;
        this.logWebSocketHandler = logWebSocketHandler;
    }

    @Bean
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    @PostConstruct
    public void start() {
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(properties.getWiremock().getPort());

        Notifier baseNotifier = properties.getWiremock().isVerbose() ? new ConsoleNotifier(true) : null;
        
        config.notifier(new Notifier() {
            @Override
            public void info(String message) {
                if (baseNotifier != null) baseNotifier.info(message);
                broadcast("INFO", message);
            }

            @Override
            public void error(String message) {
                if (baseNotifier != null) baseNotifier.error(message);
                broadcast("ERROR", "\u001B[31m" + message + "\u001B[0m");
            }

            @Override
            public void error(String message, Throwable t) {
                if (baseNotifier != null) baseNotifier.error(message, t);
                broadcast("ERROR", "\u001B[31m" + message + " - " + t.getMessage() + "\u001B[0m");
            }
        });

        // Enable proxy if configured
        if (properties.getProxy().isEnabled() && !properties.getProxy().getTargetUrl().isBlank()) {
            log.info("Configuring WireMock proxy to target: {}", properties.getProxy().getTargetUrl());
            config.enableBrowserProxying(false);
        }

        wireMockServer = new WireMockServer(config);
        
        wireMockServer.addMockServiceRequestListener((request, response) -> {
            String methodColor = getMethodColor(request.getMethod().getName());
            String statusColor = getStatusColor(response.getStatus());
            
            String logLine = String.format("%s \u001B[36m%s\u001B[0m -> %s %s", 
                    methodColor + request.getMethod().getName() + "\u001B[0m",
                    request.getUrl(),
                    statusColor + response.getStatus() + "\u001B[0m",
                    response.getStatus() >= 400 ? "\u001B[31m(Error)\u001B[0m" : "");
                    
            broadcast("REQ", logLine);
        });

        wireMockServer.start();

        // If proxy + record mode, start recording automatically
        if (properties.getProxy().isEnabled() && !properties.getProxy().getTargetUrl().isBlank()) {
            if (properties.getProxy().isRecord()) {
                log.info("Starting WireMock recording to: {}", properties.getProxy().getTargetUrl());
                wireMockServer.startRecording(
                        new RecordSpecBuilder()
                                .forTarget(properties.getProxy().getTargetUrl())
                                .ignoreRepeatRequests()
                                .makeStubsPersistent(true)
                                .build()
                );
            } else {
                // Proxy-only (no recording): set up a catch-all proxy mapping
                setupProxyMapping();
            }
        }

        int actualPort = wireMockServer.port();
        log.info("✅ WireMock server started on port {}", actualPort);
    }

    @PreDestroy
    public void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            log.info("Stopping WireMock server...");
            wireMockServer.stop();
            log.info("WireMock server stopped.");
        }
    }

    private void setupProxyMapping() {
        String targetUrl = properties.getProxy().getTargetUrl();
        wireMockServer.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.any(
                                com.github.tomakehurst.wiremock.client.WireMock.anyUrl())
                        .atPriority(Integer.MAX_VALUE) // lowest priority — only matches when no other stub does
                        .willReturn(
                                com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                                        .proxiedFrom(targetUrl))
        );
        log.info("Catch-all proxy mapping registered for unmatched requests → {}", targetUrl);
    }

    private void broadcast(String type, String message) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        String prefix = "\u001B[90m[" + time + "]\u001B[0m \u001B[33m[" + type + "]\u001B[0m ";
        logWebSocketHandler.broadcastLog("local", prefix + message);
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

    /**
     * Returns the actual port the WireMock server is listening on.
     * Useful when port is configured as 0 (random).
     */
    public int getActualPort() {
        return wireMockServer != null ? wireMockServer.port() : -1;
    }
}
