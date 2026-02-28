package io.github.vinipx.wixy.engine;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.exception.WixyException;

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
        try {
            return client.allStubMappings().getMappings();
        } catch (Exception e) {
            throw new WixyException("Failed to list stubs. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public StubMapping getStubById(UUID id) {
        try {
            return client.getStubMapping(id).getItem();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addStubMapping(StubMapping mapping) {
        try {
            client.register(mapping);
        } catch (Exception e) {
            throw new WixyException("Failed to add stub. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void editStubMapping(StubMapping mapping) {
        try {
            if (mapping.getId() != null) {
                client.removeStubMapping(mapping.getId());
            }
            client.register(mapping);
        } catch (Exception e) {
            throw new WixyException("Failed to edit stub. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeStubMapping(StubMapping mapping) {
        try {
            client.removeStubMapping(mapping);
        } catch (Exception e) {
            throw new WixyException("Failed to remove stub. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void resetStubs() {
        try {
            client.resetMappings();
        } catch (Exception e) {
            throw new WixyException("Failed to reset stubs. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void resetToDefaultMappings() {
        try {
            client.resetToDefaultMappings();
        } catch (Exception e) {
            throw new WixyException("Failed to reset to default mappings. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void stubFor(MappingBuilder mappingBuilder) {
        try {
            client.register(mappingBuilder);
        } catch (Exception e) {
            throw new WixyException("Failed to register stub. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public void startRecording(String targetUrl) {
        try {
            client.startStubRecording(
                    new RecordSpecBuilder()
                            .forTarget(targetUrl)
                            .ignoreRepeatRequests()
                            .makeStubsPersistent(true)
            );
        } catch (Exception e) {
            throw new WixyException("Failed to start recording. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public SnapshotRecordResult stopRecording() {
        try {
            return client.stopStubRecording();
        } catch (Exception e) {
            throw new WixyException("Failed to stop recording. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }

    @Override
    public RecordingStatus getRecordingStatus() {
        try {
            return client.getStubRecordingStatus().getStatus();
        } catch (Exception e) {
            throw new WixyException("Failed to get recording status. Remote engine might be unreachable: " + e.getMessage(), e);
        }
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
        try {
            client.allStubMappings();
        } catch (Exception e) {
            throw new WixyException("Failed to ping. Remote engine might be unreachable: " + e.getMessage(), e);
        }
    }
}
