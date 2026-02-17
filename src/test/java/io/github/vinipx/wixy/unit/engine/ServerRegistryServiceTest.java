package io.github.vinipx.wixy.unit.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vinipx.wixy.config.WixyProperties;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.ManagedServer;
import io.github.vinipx.wixy.engine.ServerRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("ServerRegistryService")
class ServerRegistryServiceTest {

    private WixyProperties properties;
    private EngineManager engineManager;
    private ServerRegistryService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        properties = new WixyProperties();
        String filePath = tempDir.resolve("servers.json").toString();
        properties.getRegistry().setFilePath(filePath);
        
        engineManager = mock(EngineManager.class);
        service = new ServerRegistryService(properties, objectMapper, engineManager);
        service.init();
    }

    @Test
    @DisplayName("Registry should always contain local server after init")
    void localServerPresent() {
        List<ManagedServer> servers = service.listAll();
        assertThat(servers).hasSize(1);
        assertThat(servers.get(0).getType()).isEqualTo(ManagedServer.ServerType.INTERNAL);
    }

    @Test
    @DisplayName("addServer should store and persist new server")
    void addServer() {
        ManagedServer remote = new ManagedServer(null, "Remote", "http://remote:80", ManagedServer.ServerType.REMOTE);
        ManagedServer saved = service.addServer(remote);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(service.listAll()).hasSize(2);
    }

    @Test
    @DisplayName("setActiveServer should delegate to EngineManager")
    void setActiveServer() {
        ManagedServer remote = new ManagedServer(UUID.randomUUID(), "Remote", "http://remote:80", ManagedServer.ServerType.REMOTE);
        service.addServer(remote);
        
        service.setActiveServer(remote.getId());
        verify(engineManager).switchToRemote(eq(remote.getId()), eq("http://remote:80"));
        
        service.setActiveServer(null);
        verify(engineManager).switchToLocal();
    }

    @Test
    @DisplayName("removeServer should update list")
    void removeServer() {
        ManagedServer remote = service.addServer(new ManagedServer(null, "Remote", "http://remote:80", ManagedServer.ServerType.REMOTE));
        assertThat(service.listAll()).hasSize(2);
        
        service.removeServer(remote.getId());
        assertThat(service.listAll()).hasSize(1);
    }

    @Test
    @DisplayName("getById should return the correct server")
    void getById() {
        ManagedServer remote = service.addServer(new ManagedServer(null, "Remote", "http://remote:80", ManagedServer.ServerType.REMOTE));
        assertThat(service.getById(remote.getId())).isPresent();
        assertThat(service.getById(UUID.randomUUID())).isEmpty();
    }
}
