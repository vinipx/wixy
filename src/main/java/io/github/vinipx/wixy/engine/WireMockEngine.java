package io.github.vinipx.wixy.engine;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;
import java.util.UUID;

/**
 * Interface for interacting with a WireMock instance (local or remote).
 */
public interface WireMockEngine {

    /**
     * List all active stub mappings.
     */
    List<StubMapping> listAllStubs();

    /**
     * Get a single stub mapping by its UUID.
     */
    StubMapping getStubById(UUID id);

    /**
     * Create a new stub mapping.
     */
    void addStubMapping(StubMapping mapping);

    /**
     * Update an existing stub mapping.
     */
    void editStubMapping(StubMapping mapping);

    /**
     * Delete a stub mapping by UUID.
     */
    void removeStubMapping(StubMapping mapping);

    /**
     * Remove all stub mappings.
     */
    void resetStubs();

    /**
     * Reset to default mappings (e.g. from files).
     */
    void resetToDefaultMappings();

    /**
     * Register a stub using WireMock's MappingBuilder.
     */
    void stubFor(MappingBuilder mappingBuilder);

    /**
     * Start recording traffic to a target URL.
     */
    void startRecording(String targetUrl);

    /**
     * Stop recording and return the result.
     */
    SnapshotRecordResult stopRecording();

    /**
     * Get the current recording status.
     */
    RecordingStatus getRecordingStatus();

    /**
     * Get the port the server is listening on.
     */
    int getPort();

    /**
     * Get recent serve events (the Journal).
     */
    List<com.github.tomakehurst.wiremock.stubbing.ServeEvent> listServeEvents();

    /**
     * Simple lightweight call to verify the engine is reachable.
     */
    void ping();
}
