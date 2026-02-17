package io.github.vinipx.wixy.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

/**
 * Manages the active {@link WireMockEngine} used by the services.
 */
@Component
public class EngineManager {

    private static final Logger log = LoggerFactory.getLogger(EngineManager.class);

    private final WireMockServer localWireMockServer;
    private WireMockEngine activeEngine;
    private UUID activeServerId;

    public EngineManager(WireMockServer localWireMockServer) {
        this.localWireMockServer = localWireMockServer;
        // Default to local engine
        this.activeEngine = new LocalWireMockEngine(localWireMockServer);
        this.activeServerId = null; // null means internal local
    }

    public WireMockEngine getActiveEngine() {
        return activeEngine;
    }

    public void switchToLocal() {
        this.activeEngine = new LocalWireMockEngine(localWireMockServer);
        this.activeServerId = null;
        log.info("Switched to local WireMock engine");
    }

    public void switchToRemote(UUID id, String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : (uri.getScheme().equals("https") ? 443 : 80);
            
            this.activeEngine = new RemoteWireMockEngine(host, port);
            this.activeServerId = id;
            log.info("Switched to remote WireMock engine: {} ({})", id, url);
        } catch (Exception e) {
            log.error("Failed to switch to remote engine: {}", url, e);
            throw new RuntimeException("Invalid remote URL: " + url, e);
        }
    }

    public UUID getActiveServerId() {
        return activeServerId;
    }
}
