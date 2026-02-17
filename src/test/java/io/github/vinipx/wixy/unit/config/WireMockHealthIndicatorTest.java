package io.github.vinipx.wixy.unit.config;

import io.github.vinipx.wixy.config.WireMockHealthIndicator;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.WireMockEngine;
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
    @DisplayName("When active engine is healthy")
    class Running {

        @Test
        @DisplayName("should report UP status with port and stub count")
        void upWithDetails() {
            WireMockEngine engine = mock(WireMockEngine.class);
            when(engine.getPort()).thenReturn(9090);
            when(engine.listAllStubs()).thenReturn(java.util.List.of());

            EngineManager engineManager = mock(EngineManager.class);
            when(engineManager.getActiveEngine()).thenReturn(engine);
            when(engineManager.getActiveServerId()).thenReturn(null);

            var indicator = new WireMockHealthIndicator(engineManager);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("port", 9090);
            assertThat(health.getDetails()).containsEntry("stubCount", 0);
            assertThat(health.getDetails()).containsEntry("serverId", "local");
        }

        @Test
        @DisplayName("should report correct stub count when stubs exist")
        void upWithStubs() {
            WireMockEngine engine = mock(WireMockEngine.class);
            when(engine.getPort()).thenReturn(8080);
            var stubList = java.util.List.of(
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class),
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class),
                    mock(com.github.tomakehurst.wiremock.stubbing.StubMapping.class)
            );
            when(engine.listAllStubs()).thenReturn(stubList);

            EngineManager engineManager = mock(EngineManager.class);
            when(engineManager.getActiveEngine()).thenReturn(engine);

            var indicator = new WireMockHealthIndicator(engineManager);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("stubCount", 3);
        }
    }

    @Nested
    @DisplayName("When engine communication fails")
    class NotRunning {

        @Test
        @DisplayName("should report DOWN status when exception occurs")
        void downOnException() {
            EngineManager engineManager = mock(EngineManager.class);
            when(engineManager.getActiveEngine()).thenThrow(new RuntimeException("Connection refused"));

            var indicator = new WireMockHealthIndicator(engineManager);
            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("reason", "Failed to communicate with active WireMock engine");
        }
    }
}
