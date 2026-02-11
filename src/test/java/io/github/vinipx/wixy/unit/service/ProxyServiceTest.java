package io.github.vinipx.wixy.unit.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.config.WixyProperties;
import io.github.vinipx.wixy.exception.WixyException;
import io.github.vinipx.wixy.service.ProxyService;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
@DisplayName("ProxyService")
class ProxyServiceTest {

    private static WireMockServer wireMockServer;
    private WixyProperties properties;
    private ProxyService proxyService;

    @BeforeAll static void startServer() { wireMockServer = new WireMockServer(0); wireMockServer.start(); }
    @AfterAll static void stopServer() { if (wireMockServer != null && wireMockServer.isRunning()) wireMockServer.stop(); }
    @BeforeEach void setUp() { wireMockServer.resetMappings(); properties = new WixyProperties(); proxyService = new ProxyService(wireMockServer, properties); }

    @Nested @DisplayName("getStatus()") class GetStatus {
        @Test @DisplayName("should return default status when proxy is disabled") void defaultStatus() {
            var status = proxyService.getStatus();
            assertThat(status).containsEntry("enabled", false).containsEntry("targetUrl", "").containsEntry("record", false).containsKey("wiremockPort");
            assertThat((int) status.get("wiremockPort")).isPositive();
        }
        @Test @DisplayName("should reflect enabled status after enableProxy()") void enabledStatus() {
            proxyService.enableProxy("http://example.com");
            assertThat(proxyService.getStatus()).containsEntry("enabled", true).containsEntry("targetUrl", "http://example.com");
        }
    }

    @Nested @DisplayName("enableProxy()") class EnableProxy {
        @Test @DisplayName("should enable proxy to a target URL") void validTarget() {
            proxyService.enableProxy("http://backend:8080");
            assertThat(properties.getProxy().isEnabled()).isTrue();
            assertThat(properties.getProxy().getTargetUrl()).isEqualTo("http://backend:8080");
            assertThat(wireMockServer.getStubMappings()).isNotEmpty();
        }
        @Test @DisplayName("should throw WixyException when target URL is null") void nullTarget() {
            assertThatThrownBy(() -> proxyService.enableProxy(null)).isInstanceOf(WixyException.class).hasMessageContaining("Target URL must not be blank");
        }
        @Test @DisplayName("should throw WixyException when target URL is blank") void blankTarget() {
            assertThatThrownBy(() -> proxyService.enableProxy("")).isInstanceOf(WixyException.class).hasMessageContaining("Target URL must not be blank");
        }
        @Test @DisplayName("should throw WixyException when target URL is whitespace") void whitespaceTarget() {
            assertThatThrownBy(() -> proxyService.enableProxy("   ")).isInstanceOf(WixyException.class).hasMessageContaining("Target URL must not be blank");
        }
        @Test @DisplayName("should overwrite previous proxy configuration") void overwrite() {
            proxyService.enableProxy("http://first.com"); proxyService.enableProxy("http://second.com");
            assertThat(properties.getProxy().getTargetUrl()).isEqualTo("http://second.com");
        }
    }

    @Nested @DisplayName("disableProxy()") class DisableProxy {
        @Test @DisplayName("should disable proxy and reset mappings") void disable() {
            proxyService.enableProxy("http://example.com"); assertThat(properties.getProxy().isEnabled()).isTrue();
            proxyService.disableProxy(); assertThat(properties.getProxy().isEnabled()).isFalse();
        }
        @Test @DisplayName("should be safe to call when proxy is already disabled") void idempotent() {
            assertThatCode(() -> proxyService.disableProxy()).doesNotThrowAnyException(); assertThat(properties.getProxy().isEnabled()).isFalse();
        }
    }
}
