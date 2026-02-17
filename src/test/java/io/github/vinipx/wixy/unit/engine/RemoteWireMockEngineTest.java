package io.github.vinipx.wixy.unit.engine;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.github.vinipx.wixy.engine.RemoteWireMockEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        // Mocking the result of allStubMappings() which returns a ListStubMappingsResult (package might vary by version)
        var result = mock(com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult.class);
        when(client.allStubMappings()).thenReturn(result);
        engine.listAllStubs();
        verify(client).allStubMappings();
    }

    @Test
    @DisplayName("addStubMapping should delegate to WireMock client")
    void addStubMapping() {
        StubMapping mapping = mock(StubMapping.class);
        engine.addStubMapping(mapping);
        verify(client).register(mapping);
    }

    @Test
    @DisplayName("resetStubs should delegate to WireMock client")
    void resetStubs() {
        engine.resetStubs();
        verify(client).resetMappings();
    }

    @Test
    @DisplayName("getPort should return configured port")
    void getPort() {
        assertThat(engine.getPort()).isEqualTo(8080);
    }
}
