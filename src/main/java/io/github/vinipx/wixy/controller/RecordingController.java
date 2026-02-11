package io.github.vinipx.wixy.controller;

import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import io.github.vinipx.wixy.service.RecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for WireMock recording operations.
 */
@RestController
@RequestMapping("/wixy/admin/recordings")
@Tag(name = "Recording", description = "Start/stop traffic recording")
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start recording traffic")
    public ResponseEntity<Map<String, String>> start(@RequestBody(required = false) Map<String, String> body) {
        String targetUrl = (body != null) ? body.getOrDefault("targetUrl", "") : "";
        recordingService.startRecording(targetUrl);
        return ResponseEntity.ok(Map.of("status", "Recording started"));
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop recording and save captured stubs")
    public ResponseEntity<Map<String, Object>> stop() {
        SnapshotRecordResult result = recordingService.stopRecording();
        return ResponseEntity.ok(Map.of(
                "status", "Recording stopped",
                "capturedStubs", result.getStubMappings().size()
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "Get current recording status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(recordingService.getStatus());
    }
}
