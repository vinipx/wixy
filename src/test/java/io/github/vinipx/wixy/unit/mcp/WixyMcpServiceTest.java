package io.github.vinipx.wixy.unit.mcp;

import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.mcp.WixyMcpService;
import io.github.vinipx.wixy.service.ProxyService;
import io.github.vinipx.wixy.service.RecordingService;
import io.github.vinipx.wixy.service.StubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("WixyMcpService")
class WixyMcpServiceTest {

    private final StubService stubService = mock(StubService.class);
    private final ProxyService proxyService = mock(ProxyService.class);
    private final RecordingService recordingService = mock(RecordingService.class);
    private WixyMcpService mcpService;

    @BeforeEach
    void setUp() {
        mcpService = new WixyMcpService(stubService, proxyService, recordingService);
    }

    @Nested
    @DisplayName("listStubs()")
    class ListStubs {
        @Test
        @DisplayName("should delegate to stubService.listAll()")
        void listStubs() {
            when(stubService.listAll()).thenReturn(List.of());
            String result = mcpService.listStubs();
            assertThat(result).isEqualTo("[]");
            verify(stubService).listAll();
        }
    }

    @Nested
    @DisplayName("createStub()")
    class CreateStub {
        @Test
        @DisplayName("should delegate to stubService.create()")
        void createStub() {
            String json = """
                {
                  "request": { "method": "GET" }
                }
                """;
            StubMapping mapping = mock(StubMapping.class);
            when(mapping.toString()).thenReturn("created-stub");
            when(stubService.create(json)).thenReturn(mapping);

            String result = mcpService.createStub(json);

            assertThat(result).isEqualTo("created-stub");
            verify(stubService).create(json);
        }
    }

    @Nested
    @DisplayName("deleteStub()")
    class DeleteStub {
        @Test
        @DisplayName("should delegate to stubService.delete()")
        void deleteStub() {
            UUID id = UUID.randomUUID();
            mcpService.deleteStub(id.toString());
            verify(stubService).delete(id);
        }
    }

    @Nested
    @DisplayName("enableProxy()")
    class EnableProxy {
        @Test
        @DisplayName("should delegate to proxyService.enableProxy()")
        void enableProxy() {
            String url = "http://target.com";
            mcpService.enableProxy(url);
            verify(proxyService).enableProxy(url);
        }
    }

    @Nested
    @DisplayName("disableProxy()")
    class DisableProxy {
        @Test
        @DisplayName("should delegate to proxyService.disableProxy()")
        void disableProxy() {
            mcpService.disableProxy();
            verify(proxyService).disableProxy();
        }
    }

    @Nested
    @DisplayName("startRecording()")
    class StartRecording {
        @Test
        @DisplayName("should delegate to recordingService.startRecording()")
        void startRecording() {
            String url = "http://target.com";
            mcpService.startRecording(url);
            verify(recordingService).startRecording(url);
        }
    }

    @Nested
    @DisplayName("stopRecording()")
    class StopRecording {
        @Test
        @DisplayName("should delegate to recordingService.stopRecording()")
        void stopRecording() {
            SnapshotRecordResult snapshot = mock(SnapshotRecordResult.class);
            when(snapshot.getStubMappings()).thenReturn(List.of(mock(StubMapping.class), mock(StubMapping.class)));
            when(recordingService.stopRecording()).thenReturn(snapshot);

            String result = mcpService.stopRecording();

            assertThat(result).isEqualTo("Captured 2 stubs.");
            verify(recordingService).stopRecording();
        }
    }
}
