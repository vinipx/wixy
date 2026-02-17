package io.github.vinipx.wixy.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.vinipx.wixy.config.WixyProperties;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.exception.WixyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for managing the WireMock proxy configuration at runtime.
 */
@Service
public class ProxyService {

    private static final Logger log = LoggerFactory.getLogger(ProxyService.class);

    private final EngineManager engineManager;
    private final WixyProperties properties;

    public ProxyService(EngineManager engineManager, WixyProperties properties) {
        this.engineManager = engineManager;
        this.properties = properties;
    }

    /**
     * Returns the current proxy configuration state.
     */
    public Map<String, Object> getStatus() {
        return Map.of(
                "enabled", properties.getProxy().isEnabled(),
                "targetUrl", properties.getProxy().getTargetUrl(),
                "record", properties.getProxy().isRecord(),
                "wiremockPort", engineManager.getActiveEngine().getPort()
        );
    }

    /**
     * Enable proxy mode to a given target URL at runtime.
     * This adds a catch-all proxy mapping to WireMock at the lowest priority.
     */
    public void enableProxy(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new WixyException("Target URL must not be blank");
        }

        // Add a catch-all proxy mapping at lowest priority
        engineManager.getActiveEngine().stubFor(
                WireMock.any(WireMock.anyUrl())
                        .atPriority(Integer.MAX_VALUE)
                        .willReturn(WireMock.aResponse().proxiedFrom(targetUrl))
        );

        properties.getProxy().setEnabled(true);
        properties.getProxy().setTargetUrl(targetUrl);

        log.info("Proxy enabled to: {}", targetUrl);
    }

    /**
     * Disable proxy mode by resetting the catch-all mapping.
     */
    public void disableProxy() {
        // Reset all mappings and re-add any file-based ones
        engineManager.getActiveEngine().resetToDefaultMappings();
        properties.getProxy().setEnabled(false);
        log.info("Proxy disabled; mappings reset to defaults.");
    }
}
