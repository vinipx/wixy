package io.github.vinipx.wixy.unit.controller;

import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import io.github.vinipx.wixy.controller.RecordingController;
import io.github.vinipx.wixy.exception.WixyException;
import io.github.vinipx.wixy.service.RecordingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("RecordingController")
class RecordingControllerTest {

    private final RecordingService recordingService = mock(RecordingService.class);
    private final RecordingController controller = new RecordingController(recordingService);

    @Nested @DisplayName("POST /wixy/admin/recordings/start") class Start {
        @Test @DisplayName("should return 200 with recording started message") void withTargetUrl() {
            doNothing().when(recordingService).startRecording("http://target.com");
            var body = Map.of("targetUrl", "http://target.com");
            var response = controller.start(body);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "Recording started");
            verify(recordingService).startRecording("http://target.com");
        }
        @Test @DisplayName("should pass empty string when body is null") void nullBody() {
            doNothing().when(recordingService).startRecording("");
            var response = controller.start(null);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(recordingService).startRecording("");
        }
        @Test @DisplayName("should pass empty string when targetUrl is missing from body") void missingTargetUrl() {
            doNothing().when(recordingService).startRecording("");
            var response = controller.start(Map.of());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(recordingService).startRecording("");
        }
        @Test @DisplayName("should propagate WixyException when no target available") void noTarget() {
            doThrow(new WixyException("Cannot start recording: no target URL configured.")).when(recordingService).startRecording("");
            assertThatThrownBy(() -> controller.start(null)).isInstanceOf(WixyException.class).hasMessageContaining("Cannot start recording");
        }
    }

    @Nested @DisplayName("POST /wixy/admin/recordings/stop") class Stop {
        @Test @DisplayName("should return 200 with captured stub count") void success() {
            var result = mock(SnapshotRecordResult.class);
            when(result.getStubMappings()).thenReturn(java.util.List.of());
            when(recordingService.stopRecording()).thenReturn(result);
            var response = controller.stop();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "Recording stopped").containsEntry("capturedStubs", 0);
        }
    }

    @Nested @DisplayName("GET /wixy/admin/recordings/status") class GetRecordingStatus {
        @Test @DisplayName("should return 200 with recording status") void success() {
            when(recordingService.getStatus()).thenReturn(Map.of("status", "NeverStarted"));
            var response = controller.status();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "NeverStarted");
        }
    }
}
