package io.github.vinipx.wixy.config;

import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.WireMockEngine;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports the status of the active WireMock engine.
 */
@Component("wiremock")
public class WireMockHealthIndicator implements HealthIndicator {

    private final EngineManager engineManager;

    public WireMockHealthIndicator(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public Health health() {
        try {
            WireMockEngine engine = engineManager.getActiveEngine();
            int port = engine.getPort();
            int stubCount = engine.listAllStubs().size();
            
            return Health.up()
                    .withDetail("port", port)
                    .withDetail("stubCount", stubCount)
                    .withDetail("serverId", engineManager.getActiveServerId() == null ? "local" : engineManager.getActiveServerId())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("reason", "Failed to communicate with active WireMock engine")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
