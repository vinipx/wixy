package io.github.vinipx.wixy.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates and manages the lifecycle of the embedded {@link WireMockServer}.
 */
@Configuration
public class WireMockConfig {

    private static final Logger log = LoggerFactory.getLogger(WireMockConfig.class);

    private final WixyProperties properties;
    private WireMockServer wireMockServer;

    public WireMockConfig(WixyProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

    @PostConstruct
    public void start() {
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(properties.getWiremock().getPort());

        if (properties.getWiremock().isVerbose()) {
            config.notifier(new ConsoleNotifier(true));
        }

        // Enable proxy if configured
        if (properties.getProxy().isEnabled() && !properties.getProxy().getTargetUrl().isBlank()) {
            log.info("Configuring WireMock proxy to target: {}", properties.getProxy().getTargetUrl());
            config.enableBrowserProxying(false);
        }

        wireMockServer = new WireMockServer(config);
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

    /**
     * Returns the actual port the WireMock server is listening on.
     * Useful when port is configured as 0 (random).
     */
    public int getActualPort() {
        return wireMockServer != null ? wireMockServer.port() : -1;
    }
}
