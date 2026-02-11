package io.github.vinipx.wixy.unit.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.config.WireMockConfig;
import io.github.vinipx.wixy.config.WixyProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link WireMockConfig} — validates lifecycle management,
 * port allocation, and proxy mapping setup.
 */
@Tag("unit")
@DisplayName("WireMockConfig")
class WireMockConfigTest {

    private WixyProperties defaultProperties() {
        var props = new WixyProperties();
        props.getWiremock().setPort(0); // random port
        props.getWiremock().setVerbose(false);
        return props;
    }

    // ── Lifecycle ───────────────────────────────────────────────

    @Nested
    @DisplayName("Lifecycle management")
    class Lifecycle {

        @Test
        @DisplayName("should start WireMock on PostConstruct and stop on PreDestroy")
        void startAndStop() {
            var config = new WireMockConfig(defaultProperties());
            config.start();

            WireMockServer server = config.wireMockServer();
            assertThat(server).isNotNull();
            assertThat(server.isRunning()).isTrue();
            assertThat(config.getActualPort()).isPositive();

            config.stop();
            assertThat(server.isRunning()).isFalse();
        }

        @Test
        @DisplayName("stop should be idempotent when server already stopped")
        void stopIdempotent() {
            var config = new WireMockConfig(defaultProperties());
            config.start();
            config.stop();

            // Second stop should not throw
            assertThatCode(config::stop).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getActualPort should return -1 before start")
        void portBeforeStart() {
            var config = new WireMockConfig(defaultProperties());
            assertThat(config.getActualPort()).isEqualTo(-1);
        }
    }

    // ── Random port allocation ──────────────────────────────────

    @Nested
    @DisplayName("Port allocation")
    class PortAllocation {

        @Test
        @DisplayName("should allocate random port when configured as 0")
        void randomPort() {
            var config = new WireMockConfig(defaultProperties());
            config.start();
            try {
                assertThat(config.getActualPort()).isGreaterThan(0);
                assertThat(config.getActualPort()).isLessThanOrEqualTo(65535);
            } finally {
                config.stop();
            }
        }

        @Test
        @DisplayName("two instances should get different random ports")
        void differentRandomPorts() {
            var config1 = new WireMockConfig(defaultProperties());
            var config2 = new WireMockConfig(defaultProperties());
            config1.start();
            config2.start();
            try {
                assertThat(config1.getActualPort()).isNotEqualTo(config2.getActualPort());
            } finally {
                config1.stop();
                config2.stop();
            }
        }
    }

    // ── Verbose mode ────────────────────────────────────────────

    @Nested
    @DisplayName("Verbose mode")
    class VerboseMode {

        @Test
        @DisplayName("should start with verbose notifier when enabled")
        void verboseEnabled() {
            var props = defaultProperties();
            props.getWiremock().setVerbose(true);
            var config = new WireMockConfig(props);
            config.start();
            try {
                assertThat(config.wireMockServer().isRunning()).isTrue();
            } finally {
                config.stop();
            }
        }

        @Test
        @DisplayName("should start without verbose notifier when disabled")
        void verboseDisabled() {
            var props = defaultProperties();
            props.getWiremock().setVerbose(false);
            var config = new WireMockConfig(props);
            config.start();
            try {
                assertThat(config.wireMockServer().isRunning()).isTrue();
            } finally {
                config.stop();
            }
        }
    }

    // ── Proxy setup ─────────────────────────────────────────────

    @Nested
    @DisplayName("Proxy configuration")
    class ProxySetup {

        @Test
        @DisplayName("should not set up proxy when proxy is disabled")
        void proxyDisabled() {
            var props = defaultProperties();
            props.getProxy().setEnabled(false);
            var config = new WireMockConfig(props);
            config.start();
            try {
                // No catch-all proxy stub should be added
                assertThat(config.wireMockServer().getStubMappings()).isEmpty();
            } finally {
                config.stop();
            }
        }

        @Test
        @DisplayName("should set up catch-all proxy when enabled without record")
        void proxyEnabledNoRecord() {
            var props = defaultProperties();
            props.getProxy().setEnabled(true);
            props.getProxy().setTargetUrl("http://httpbin.org");
            props.getProxy().setRecord(false);
            var config = new WireMockConfig(props);
            config.start();
            try {
                // Should have exactly one catch-all proxy mapping
                assertThat(config.wireMockServer().getStubMappings()).hasSize(1);
            } finally {
                config.stop();
            }
        }

        @Test
        @DisplayName("should not set up proxy when target URL is blank")
        void proxyEnabledBlankTarget() {
            var props = defaultProperties();
            props.getProxy().setEnabled(true);
            props.getProxy().setTargetUrl("");
            var config = new WireMockConfig(props);
            config.start();
            try {
                assertThat(config.wireMockServer().getStubMappings()).isEmpty();
            } finally {
                config.stop();
            }
        }
    }

    // ── Bean exposure ───────────────────────────────────────────

    @Nested
    @DisplayName("Bean wiring")
    class BeanExposure {

        @Test
        @DisplayName("wireMockServer() bean should return the same running instance")
        void beanIdentity() {
            var config = new WireMockConfig(defaultProperties());
            config.start();
            try {
                WireMockServer bean = config.wireMockServer();
                assertThat(bean).isNotNull();
                assertThat(bean.isRunning()).isTrue();
                assertThat(bean.port()).isEqualTo(config.getActualPort());
            } finally {
                config.stop();
            }
        }
    }
}
