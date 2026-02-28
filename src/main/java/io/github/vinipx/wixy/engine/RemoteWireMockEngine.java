package io.github.vinipx.wixy.engine;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link WireMockEngine} that interacts with a remote WireMock instance via its Admin API.
 */
public class RemoteWireMockEngine implements WireMockEngine {

    private final WireMock client;
    private final int port;

    public RemoteWireMockEngine(String host, int port) {
        this.client = new WireMock(host, port);
        this.port = port;
    }

    @Override
    public List<StubMapping> listAllStubs() {
        return client.allStubMappings().getMappings();
    }

    @Override
    public StubMapping getStubById(UUID id) {
        return client.getStubMapping(id).getItem();
    }

    @Override
    public void addStubMapping(StubMapping mapping) {
        client.register(mapping);
    }

    @Override
    public void editStubMapping(StubMapping mapping) {
        client.register(mapping); // WireMock's register handles both create and update if ID is present
    }

    @Override
    public void removeStubMapping(StubMapping mapping) {
        client.removeStubMapping(mapping);
    }

    @Override
    public void resetStubs() {
        client.resetMappings();
    }

    @Override
    public void resetToDefaultMappings() {
        client.resetToDefaultMappings();
    }

    @Override
    public void stubFor(MappingBuilder mappingBuilder) {
        client.register(mappingBuilder);
    }

    @Override
    public void startRecording(String targetUrl) {
        client.startRecording(
                new RecordSpecBuilder()
                        .forTarget(targetUrl)
                        .ignoreRepeatRequests()
                        .makeStubsPersistent(true)
        );
    }

    @Override
    public SnapshotRecordResult stopRecording() {
        return client.stopRecording();
    }

    @Override
    public RecordingStatus getRecordingStatus() {
        return client.getRecordingStatus().getStatus();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public List<com.github.tomakehurst.wiremock.stubbing.ServeEvent> listServeEvents() {
        return client.getServeEvents();
    }

    @Override
    public void ping() {
        // A simple lightweight call to verify connection
        client.allStubMappings();
    }
}
