package io.github.vinipx.wixy.unit.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.engine.LocalWireMockEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("LocalWireMockEngine")
class LocalWireMockEngineTest {

    private final WireMockServer server = mock(WireMockServer.class);
    private final LocalWireMockEngine engine = new LocalWireMockEngine(server);

    @Test
    @DisplayName("listAllStubs should delegate to WireMockServer")
    void listAllStubs() {
        when(server.getStubMappings()).thenReturn(List.of());
        engine.listAllStubs();
        verify(server).getStubMappings();
    }

    @Test
    @DisplayName("getStubById should delegate to WireMockServer")
    void getStubById() {
        UUID id = UUID.randomUUID();
        engine.getStubById(id);
        verify(server).getSingleStubMapping(id);
    }

    @Test
    @DisplayName("resetStubs should delegate to WireMockServer")
    void resetStubs() {
        engine.resetStubs();
        verify(server).resetMappings();
    }

    @Test
    @DisplayName("getPort should delegate to WireMockServer")
    void getPort() {
        when(server.port()).thenReturn(9090);
        assertThat(engine.getPort()).isEqualTo(9090);
    }

    @Test
    @DisplayName("getRecordingStatus should delegate to WireMockServer")
    void getRecordingStatus() {
        RecordingStatusResult statusResult = mock(RecordingStatusResult.class);
        when(statusResult.getStatus()).thenReturn(RecordingStatus.Recording);
        when(server.getRecordingStatus()).thenReturn(statusResult);
        
        assertThat(engine.getRecordingStatus()).isEqualTo(RecordingStatus.Recording);
    }
}
