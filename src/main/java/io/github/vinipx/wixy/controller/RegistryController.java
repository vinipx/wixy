package io.github.vinipx.wixy.controller;

import io.github.vinipx.wixy.engine.ManagedServer;
import io.github.vinipx.wixy.engine.ServerRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for managing the registry of WireMock servers.
 */
@RestController
@RequestMapping("/wixy/admin/registry")
@Tag(name = "Registry Management", description = "Manage the registry of WireMock servers")
public class RegistryController {

    private final ServerRegistryService registryService;

    public RegistryController(ServerRegistryService registryService) {
        this.registryService = registryService;
    }

    @GetMapping("/servers")
    @Operation(summary = "List all managed servers")
    public List<ManagedServer> listServers() {
        return registryService.listAll();
    }

    @PostMapping("/servers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new remote server to the registry")
    public ManagedServer addServer(@RequestBody ManagedServer server) {
        server.setType(ManagedServer.ServerType.REMOTE);
        return registryService.addServer(server);
    }

    @DeleteMapping("/servers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a server from the registry")
    public void removeServer(@PathVariable UUID id) {
        registryService.removeServer(id);
    }

    @PostMapping("/active")
    @Operation(summary = "Set the active server for Wixy to manage")
    public Map<String, Object> setActive(@RequestBody Map<String, String> request) {
        String idStr = request.get("id");
        UUID id = (idStr == null || idStr.isBlank() || idStr.equalsIgnoreCase("local")) ? null : UUID.fromString(idStr);
        registryService.setActiveServer(id);
        return Map.of("status", "Active server switched", "activeServerId", id == null ? "local" : id);
    }

    @GetMapping("/active")
    @Operation(summary = "Get the ID of the currently active server")
    public Map<String, Object> getActive() {
        UUID activeId = registryService.getActiveServerId();
        return Map.of("activeServerId", activeId == null ? "local" : activeId);
    }
}
