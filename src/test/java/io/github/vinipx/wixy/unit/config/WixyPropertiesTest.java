package io.github.vinipx.wixy.unit.config;

import io.github.vinipx.wixy.config.WixyProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link WixyProperties} — validates defaults, setters,
 * constraints, and nested property binding.
 */
@Tag("unit")
@DisplayName("WixyProperties")
class WixyPropertiesTest {

    // ── Defaults ────────────────────────────────────────────────

    @Nested
    @DisplayName("Default values")
    class Defaults {

        private final WixyProperties props = new WixyProperties();

        @Test
        @DisplayName("should have non-null wiremock section")
        void wiremockSectionNotNull() {
            assertThat(props.getWiremock()).isNotNull();
        }

        @Test
        @DisplayName("should have non-null proxy section")
        void proxySectionNotNull() {
            assertThat(props.getProxy()).isNotNull();
        }

        @Test
        @DisplayName("should have non-null security section")
        void securitySectionNotNull() {
            assertThat(props.getSecurity()).isNotNull();
        }

        @Test
        @DisplayName("wiremock.port defaults to 9090")
        void wiremockPortDefault() {
            assertThat(props.getWiremock().getPort()).isEqualTo(9090);
        }

        @Test
        @DisplayName("wiremock.verbose defaults to true")
        void wiremockVerboseDefault() {
            assertThat(props.getWiremock().isVerbose()).isTrue();
        }

        @Test
        @DisplayName("wiremock.rootDir defaults to classpath:/wiremock")
        void wiremockRootDirDefault() {
            assertThat(props.getWiremock().getRootDir()).isEqualTo("classpath:/wiremock");
        }

        @Test
        @DisplayName("proxy.enabled defaults to false")
        void proxyEnabledDefault() {
            assertThat(props.getProxy().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("proxy.targetUrl defaults to empty")
        void proxyTargetUrlDefault() {
            assertThat(props.getProxy().getTargetUrl()).isEmpty();
        }

        @Test
        @DisplayName("proxy.record defaults to false")
        void proxyRecordDefault() {
            assertThat(props.getProxy().isRecord()).isFalse();
        }

        @Test
        @DisplayName("security.enabled defaults to false")
        void securityEnabledDefault() {
            assertThat(props.getSecurity().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("security.apiKey defaults to empty")
        void securityApiKeyDefault() {
            assertThat(props.getSecurity().getApiKey()).isEmpty();
        }
    }

    // ── Setters ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Setter mutations")
    class Setters {

        @Test
        @DisplayName("should set wiremock port")
        void setWiremockPort() {
            var props = new WixyProperties();
            props.getWiremock().setPort(8888);
            assertThat(props.getWiremock().getPort()).isEqualTo(8888);
        }

        @Test
        @DisplayName("should set wiremock verbose")
        void setWiremockVerbose() {
            var props = new WixyProperties();
            props.getWiremock().setVerbose(false);
            assertThat(props.getWiremock().isVerbose()).isFalse();
        }

        @Test
        @DisplayName("should set wiremock rootDir")
        void setWiremockRootDir() {
            var props = new WixyProperties();
            props.getWiremock().setRootDir("/custom/path");
            assertThat(props.getWiremock().getRootDir()).isEqualTo("/custom/path");
        }

        @Test
        @DisplayName("should set proxy enabled")
        void setProxyEnabled() {
            var props = new WixyProperties();
            props.getProxy().setEnabled(true);
            assertThat(props.getProxy().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should set proxy targetUrl")
        void setProxyTargetUrl() {
            var props = new WixyProperties();
            props.getProxy().setTargetUrl("https://api.example.com");
            assertThat(props.getProxy().getTargetUrl()).isEqualTo("https://api.example.com");
        }

        @Test
        @DisplayName("should set proxy record")
        void setProxyRecord() {
            var props = new WixyProperties();
            props.getProxy().setRecord(true);
            assertThat(props.getProxy().isRecord()).isTrue();
        }

        @Test
        @DisplayName("should set security enabled")
        void setSecurityEnabled() {
            var props = new WixyProperties();
            props.getSecurity().setEnabled(true);
            assertThat(props.getSecurity().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("should set security apiKey")
        void setSecurityApiKey() {
            var props = new WixyProperties();
            props.getSecurity().setApiKey("my-secret");
            assertThat(props.getSecurity().getApiKey()).isEqualTo("my-secret");
        }

        @Test
        @DisplayName("should replace entire wiremock section")
        void replaceWiremockSection() {
            var props = new WixyProperties();
            var wm = new WixyProperties.Wiremock();
            wm.setPort(7777);
            wm.setVerbose(false);
            props.setWiremock(wm);

            assertThat(props.getWiremock().getPort()).isEqualTo(7777);
            assertThat(props.getWiremock().isVerbose()).isFalse();
        }

        @Test
        @DisplayName("should replace entire proxy section")
        void replaceProxySection() {
            var props = new WixyProperties();
            var proxy = new WixyProperties.Proxy();
            proxy.setEnabled(true);
            proxy.setTargetUrl("http://backend:8080");
            proxy.setRecord(true);
            props.setProxy(proxy);

            assertThat(props.getProxy().isEnabled()).isTrue();
            assertThat(props.getProxy().getTargetUrl()).isEqualTo("http://backend:8080");
            assertThat(props.getProxy().isRecord()).isTrue();
        }

        @Test
        @DisplayName("should replace entire security section")
        void replaceSecuritySection() {
            var props = new WixyProperties();
            var sec = new WixyProperties.Security();
            sec.setEnabled(true);
            sec.setApiKey("super-secret");
            props.setSecurity(sec);

            assertThat(props.getSecurity().isEnabled()).isTrue();
            assertThat(props.getSecurity().getApiKey()).isEqualTo("super-secret");
        }
    }

    // ── Nested class isolation ──────────────────────────────────

    @Nested
    @DisplayName("Nested class independence")
    class NestedClassIsolation {

        @Test
        @DisplayName("Wiremock instances should be independent")
        void wiremockIndependence() {
            var a = new WixyProperties.Wiremock();
            var b = new WixyProperties.Wiremock();
            a.setPort(1111);
            assertThat(b.getPort()).isEqualTo(9090);
        }

        @Test
        @DisplayName("Proxy instances should be independent")
        void proxyIndependence() {
            var a = new WixyProperties.Proxy();
            var b = new WixyProperties.Proxy();
            a.setEnabled(true);
            assertThat(b.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Security instances should be independent")
        void securityIndependence() {
            var a = new WixyProperties.Security();
            var b = new WixyProperties.Security();
            a.setApiKey("key-a");
            assertThat(b.getApiKey()).isEmpty();
        }
    }
}
