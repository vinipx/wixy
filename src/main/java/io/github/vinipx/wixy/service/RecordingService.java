package io.github.vinipx.wixy.service;

import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import io.github.vinipx.wixy.config.WixyProperties;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.exception.WixyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for starting/stopping WireMock traffic recording.
 */
@Service
public class RecordingService {

    private static final Logger log = LoggerFactory.getLogger(RecordingService.class);

    private final EngineManager engineManager;
    private final WixyProperties properties;

    public RecordingService(EngineManager engineManager, WixyProperties properties) {
        this.engineManager = engineManager;
        this.properties = properties;
    }

    /**
     * Start recording traffic to the given target URL.
     *
     * @param targetUrl the upstream URL to record from. If null/blank, falls back to configured target.
     */
    public void startRecording(String targetUrl) {
        String effectiveTarget = (targetUrl != null && !targetUrl.isBlank())
                ? targetUrl
                : properties.getProxy().getTargetUrl();

        if (effectiveTarget == null || effectiveTarget.isBlank()) {
            throw new WixyException("Cannot start recording: no target URL configured. "
                    + "Set wixy.proxy.target-url or provide targetUrl in the request body.");
        }

        log.info("Starting recording to: {}", effectiveTarget);
        engineManager.getActiveEngine().startRecording(effectiveTarget);
    }

    /**
     * Stop recording and return the list of captured stub mappings.
     */
    public SnapshotRecordResult stopRecording() {
        log.info("Stopping recording...");
        SnapshotRecordResult result = engineManager.getActiveEngine().stopRecording();
        log.info("Recording stopped. Captured {} stub(s).", result.getStubMappings().size());
        return result;
    }

    /**
     * Get the current recording status.
     */
    public Map<String, Object> getStatus() {
        RecordingStatus status = engineManager.getActiveEngine().getRecordingStatus();
        return Map.of(
                "status", status.name()
        );
    }
}
