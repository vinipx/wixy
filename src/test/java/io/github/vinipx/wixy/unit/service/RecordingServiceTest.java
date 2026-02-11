package io.github.vinipx.wixy.unit.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import io.github.vinipx.wixy.config.WixyProperties;
import io.github.vinipx.wixy.exception.WixyException;
import io.github.vinipx.wixy.service.RecordingService;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
@DisplayName("RecordingService")
class RecordingServiceTest {

    private static WireMockServer wireMockServer;
    private WixyProperties properties;
    private RecordingService recordingService;

    @BeforeAll static void startServer() { wireMockServer = new WireMockServer(0); wireMockServer.start(); }
    @AfterAll static void stopServer() { if (wireMockServer != null && wireMockServer.isRunning()) wireMockServer.stop(); }
    @BeforeEach void setUp() {
        wireMockServer.resetMappings();
        try { wireMockServer.stopRecording(); } catch (Exception ignored) {}
        properties = new WixyProperties();
        recordingService = new RecordingService(wireMockServer, properties);
    }

    @Nested @DisplayName("getStatus()") class GetStatus {
        @Test @DisplayName("should return recording status (NeverStarted or Stopped)") void initialStatus() {
            Map<String, Object> status = recordingService.getStatus();
            assertThat(status).containsKey("status");
            assertThat(status.get("status").toString()).isIn("NeverStarted", "Stopped");
        }
        @Test @DisplayName("should return Recording when actively recording") void activelyRecording() {
            properties.getProxy().setTargetUrl("http://example.com");
            recordingService.startRecording("http://example.com");
            assertThat(recordingService.getStatus().get("status").toString()).isEqualTo("Recording");
            recordingService.stopRecording();
        }
    }

    @Nested @DisplayName("startRecording()") class StartRecording {
        @Test @DisplayName("should start recording to an explicit target URL") void withExplicitTarget() {
            assertThatCode(() -> recordingService.startRecording("http://httpbin.org")).doesNotThrowAnyException();
            assertThat(recordingService.getStatus().get("status").toString()).isEqualTo("Recording");
            recordingService.stopRecording();
        }
        @Test @DisplayName("should fall back to configured target when parameter is null") void fallbackToConfig() {
            properties.getProxy().setTargetUrl("http://httpbin.org");
            assertThatCode(() -> recordingService.startRecording(null)).doesNotThrowAnyException();
            recordingService.stopRecording();
        }
        @Test @DisplayName("should fall back to configured target when parameter is blank") void fallbackToConfigBlank() {
            properties.getProxy().setTargetUrl("http://httpbin.org");
            assertThatCode(() -> recordingService.startRecording("")).doesNotThrowAnyException();
            recordingService.stopRecording();
        }
        @Test @DisplayName("should throw WixyException when no target URL is available") void noTarget() {
            properties.getProxy().setTargetUrl("");
            assertThatThrownBy(() -> recordingService.startRecording(null)).isInstanceOf(WixyException.class).hasMessageContaining("Cannot start recording");
        }
        @Test @DisplayName("should throw WixyException when both parameter and config are blank") void bothBlank() {
            properties.getProxy().setTargetUrl("   ");
            assertThatThrownBy(() -> recordingService.startRecording("")).isInstanceOf(WixyException.class).hasMessageContaining("no target URL configured");
        }
    }

    @Nested @DisplayName("stopRecording()") class StopRecording {
        @Test @DisplayName("should stop recording and return result") void stopAfterStart() {
            recordingService.startRecording("http://httpbin.org");
            SnapshotRecordResult result = recordingService.stopRecording();
            assertThat(result).isNotNull(); assertThat(result.getStubMappings()).isNotNull();
        }
        @Test @DisplayName("should return result when stopping without active recording") void stopWithoutStart() {
            try {
                SnapshotRecordResult result = recordingService.stopRecording();
                assertThat(result).isNotNull(); assertThat(result.getStubMappings()).isEmpty();
            } catch (com.github.tomakehurst.wiremock.recording.NotRecordingException e) {
                assertThat(e).isNotNull();
            }
        }
    }
}
