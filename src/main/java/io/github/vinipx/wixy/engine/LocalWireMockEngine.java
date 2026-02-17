package io.github.vinipx.wixy.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link WireMockEngine} that interacts with an embedded {@link WireMockServer}.
 */
public class LocalWireMockEngine implements WireMockEngine {

    private final WireMockServer wireMockServer;

    public LocalWireMockEngine(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    @Override
    public List<StubMapping> listAllStubs() {
        return wireMockServer.getStubMappings();
    }

    @Override
    public StubMapping getStubById(UUID id) {
        return wireMockServer.getSingleStubMapping(id);
    }

    @Override
    public void addStubMapping(StubMapping mapping) {
        wireMockServer.addStubMapping(mapping);
    }

    @Override
    public void editStubMapping(StubMapping mapping) {
        wireMockServer.editStubMapping(mapping);
    }

    @Override
    public void removeStubMapping(StubMapping mapping) {
        wireMockServer.removeStubMapping(mapping);
    }

    @Override
    public void resetStubs() {
        wireMockServer.resetMappings();
    }

    @Override
    public void resetToDefaultMappings() {
        wireMockServer.resetToDefaultMappings();
    }

    @Override
    public void stubFor(MappingBuilder mappingBuilder) {
        wireMockServer.stubFor(mappingBuilder);
    }

    @Override
    public void startRecording(String targetUrl) {
        wireMockServer.startRecording(
                new RecordSpecBuilder()
                        .forTarget(targetUrl)
                        .ignoreRepeatRequests()
                        .makeStubsPersistent(true)
                        .build()
        );
    }

    @Override
    public SnapshotRecordResult stopRecording() {
        return wireMockServer.stopRecording();
    }

    @Override
    public RecordingStatus getRecordingStatus() {
        return wireMockServer.getRecordingStatus().getStatus();
    }

    @Override
    public int getPort() {
        return wireMockServer.port();
    }

    @Override
    public void ping() {
        if (!wireMockServer.isRunning()) {
            throw new IllegalStateException("Local WireMock server is not running");
        }
    }
}
