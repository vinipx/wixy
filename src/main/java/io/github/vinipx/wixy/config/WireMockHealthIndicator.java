package io.github.vinipx.wixy.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports the status of the embedded WireMock server.
 */
@Component("wiremock")
public class WireMockHealthIndicator implements HealthIndicator {

    private final WireMockServer wireMockServer;

    public WireMockHealthIndicator(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    @Override
    public Health health() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            return Health.up()
                    .withDetail("port", wireMockServer.port())
                    .withDetail("stubCount", wireMockServer.getStubMappings().size())
                    .build();
        }
        return Health.down()
                .withDetail("reason", "WireMock server is not running")
                .build();
    }
}
