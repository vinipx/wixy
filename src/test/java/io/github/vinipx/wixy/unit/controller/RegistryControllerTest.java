package io.github.vinipx.wixy.unit.controller;

import io.github.vinipx.wixy.controller.RegistryController;
import io.github.vinipx.wixy.engine.ManagedServer;
import io.github.vinipx.wixy.engine.ServerRegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("RegistryController")
class RegistryControllerTest {

    private final ServerRegistryService registryService = mock(ServerRegistryService.class);
    private final RegistryController controller = new RegistryController(registryService);

    @Test
    @DisplayName("listServers should return list from service")
    void listServers() {
        when(registryService.listAll()).thenReturn(List.of());
        assertThat(controller.listServers()).isEmpty();
        verify(registryService).listAll();
    }

    @Test
    @DisplayName("addServer should delegate to service and set type to REMOTE")
    void addServer() {
        ManagedServer server = new ManagedServer();
        when(registryService.addServer(any())).thenReturn(server);
        
        ManagedServer result = controller.addServer(server);
        assertThat(server.getType()).isEqualTo(ManagedServer.ServerType.REMOTE);
        assertThat(result).isEqualTo(server);
    }

    @Test
    @DisplayName("setActive should delegate to service")
    void setActive() {
        UUID id = UUID.randomUUID();
        controller.setActive(Map.of("id", id.toString()));
        verify(registryService).setActiveServer(id);
        
        controller.setActive(Map.of("id", "local"));
        verify(registryService).setActiveServer(null);
    }

    @Test
    @DisplayName("getActive should return active ID from service")
    void getActive() {
        UUID id = UUID.randomUUID();
        when(registryService.getActiveServerId()).thenReturn(id);
        
        Map<String, Object> result = controller.getActive();
        assertThat(result).containsEntry("activeServerId", id);
    }
}
