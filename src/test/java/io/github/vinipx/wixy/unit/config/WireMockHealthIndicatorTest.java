package io.github.vinipx.wixy.unit.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.vinipx.wixy.config.WireMockHealthIndicator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WireMockHealthIndicator}.
 */
@Tag("unit")
@DisplayName("WireMockHealthIndicator")
class WireMockHealthIndicatorTest {

    @Nested
    @DisplayName("When WireMock is running")
    class Running {

        @Test
        @DisplayName("should report UP status with port and stub count")
        void upWithDetails() {
            WireMockServer server = mock(WireMockServer.class);
            when(server.isRunning()).thenReturn(true);
            when(server.port()).thenReturn(9090);
            when(server.getStubMappings()).thenReturn(java.util.List.of());

            var indicator = new WireMockHealthIndicator(server);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("port", 9090);
            assertThat(health.getDetails()).containsEntry("stubCount", 0);
        }

        @Test
        @DisplayName("should report correct stub count when stubs exist")
        void upWithStubs() {
            WireMockServer server = mock(WireMockServer.class);
            when(server.isRunning()).thenReturn(true);
            when(server.port()).thenReturn(8080);
            var stubList = java.util.List.of(
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class),
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class),
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class)
            );
            when(server.getStubMappings()).thenReturn(stubList);

            var indicator = new WireMockHealthIndicator(server);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("stubCount", 3);
        }
    }

    @Nested
    @DisplayName("When WireMock is not running")
    class NotRunning {

        @Test
        @DisplayName("should report DOWN status when server is stopped")
        void downWhenStopped() {
            WireMockServer server = mock(WireMockServer.class);
            when(server.isRunning()).thenReturn(false);

            var indicator = new WireMockHealthIndicator(server);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("reason", "WireMock server is not running");
        }

        @Test
        @DisplayName("should report DOWN status when server is null")
        void downWhenNull() {
            // Passing null to simulate uninitialized state
            var indicator = new WireMockHealthIndicator(null);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }
    }
}
