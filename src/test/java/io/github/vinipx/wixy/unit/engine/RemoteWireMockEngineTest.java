package io.github.vinipx.wixy.unit.engine;

import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.engine.RemoteWireMockEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("RemoteWireMockEngine")
class RemoteWireMockEngineTest {

    private WireMock client;
    private RemoteWireMockEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RemoteWireMockEngine("localhost", 8080);
        client = mock(WireMock.class);
        // Inject mock client via reflection since it's private final
        ReflectionTestUtils.setField(engine, "client", client);
    }

    @Test
    @DisplayName("listAllStubs should delegate to WireMock client")
    void listAllStubs() {
        var result = mock(ListStubMappingsResult.class);
        when(client.allStubMappings()).thenReturn(result);
        engine.listAllStubs();
        verify(client).allStubMappings();
    }

    @Test
    @DisplayName("getStubById should delegate to WireMock client")
    void getStubById() {
        UUID id = UUID.randomUUID();
        var result = mock(SingleStubMappingResult.class);
        StubMapping mapping = mock(StubMapping.class);
        when(result.getItem()).thenReturn(mapping);
        when(client.getStubMapping(id)).thenReturn(result);

        StubMapping retrieved = engine.getStubById(id);

        verify(client).getStubMapping(id);
        assertThat(retrieved).isSameAs(mapping);
    }

    @Test
    @DisplayName("addStubMapping should delegate to WireMock client")
    void addStubMapping() {
        StubMapping mapping = mock(StubMapping.class);
        engine.addStubMapping(mapping);
        verify(client).register(mapping);
    }

    @Test
    @DisplayName("editStubMapping should delegate to WireMock client via register")
    void editStubMapping() {
        StubMapping mapping = mock(StubMapping.class);
        UUID id = UUID.randomUUID();
        when(mapping.getId()).thenReturn(id);
        engine.editStubMapping(mapping);
        verify(client).removeStubMapping(id);
        verify(client).register(mapping);
    }

    @Test
    @DisplayName("removeStubMapping should delegate to WireMock client")
    void removeStubMapping() {
        StubMapping mapping = mock(StubMapping.class);
        engine.removeStubMapping(mapping);
        verify(client).removeStubMapping(mapping);
    }

    @Test
    @DisplayName("resetStubs should delegate to WireMock client")
    void resetStubs() {
        engine.resetStubs();
        verify(client).resetMappings();
    }

    @Test
    @DisplayName("resetToDefaultMappings should delegate to WireMock client")
    void resetToDefaultMappings() {
        engine.resetToDefaultMappings();
        verify(client).resetToDefaultMappings();
    }

    @Test
    @DisplayName("stubFor should delegate to WireMock client")
    void stubFor() {
        MappingBuilder mappingBuilder = mock(MappingBuilder.class);
        engine.stubFor(mappingBuilder);
        verify(client).register(mappingBuilder);
    }

    @Test
    @DisplayName("startRecording should delegate to WireMock client")
    void startRecording() {
        engine.startRecording("http://example.com");
        verify(client).startStubRecording(any(com.github.tomakehurst.wiremock.recording.RecordSpecBuilder.class));
    }

    @Test
    @DisplayName("stopRecording should delegate to WireMock client")
    void stopRecording() {
        SnapshotRecordResult result = mock(SnapshotRecordResult.class);
        when(client.stopStubRecording()).thenReturn(result);

        SnapshotRecordResult actual = engine.stopRecording();

        verify(client).stopStubRecording();
        assertThat(actual).isSameAs(result);
    }

    @Test
    @DisplayName("getRecordingStatus should delegate to WireMock client")
    void getRecordingStatus() {
        RecordingStatusResult result = mock(RecordingStatusResult.class);
        when(result.getStatus()).thenReturn(com.github.tomakehurst.wiremock.recording.RecordingStatus.Recording);
        when(client.getStubRecordingStatus()).thenReturn(result);

        com.github.tomakehurst.wiremock.recording.RecordingStatus actual = engine.getRecordingStatus();

        verify(client).getStubRecordingStatus();
        assertThat(actual).isEqualTo(com.github.tomakehurst.wiremock.recording.RecordingStatus.Recording);
    }

    @Test
    @DisplayName("getPort should return configured port")
    void getPort() {
        assertThat(engine.getPort()).isEqualTo(8080);
    }
}
