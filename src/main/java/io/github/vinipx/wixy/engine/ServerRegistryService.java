package io.github.vinipx.wixy.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vinipx.wixy.config.WixyProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing the registry of WireMock servers.
 */
@Service
public class ServerRegistryService {

    private static final Logger log = LoggerFactory.getLogger(ServerRegistryService.class);

    private final WixyProperties properties;
    private final ObjectMapper objectMapper;
    private final EngineManager engineManager;
    private List<ManagedServer> servers = new ArrayList<>();

    public ServerRegistryService(WixyProperties properties, ObjectMapper objectMapper, EngineManager engineManager) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.engineManager = engineManager;
    }

    @PostConstruct
    public void init() {
        loadServers();
        // Ensure local server is always in the list
        if (servers.stream().noneMatch(s -> s.getType() == ManagedServer.ServerType.INTERNAL)) {
            servers.add(0, new ManagedServer(null, "Local Embedded Server", "http://localhost:" + properties.getWiremock().getPort(), ManagedServer.ServerType.INTERNAL));
            saveServers();
        }
    }

    public List<ManagedServer> listAll() {
        return new ArrayList<>(servers);
    }

    public ManagedServer addServer(ManagedServer server) {
        if (server.getId() == null) {
            server.setId(UUID.randomUUID());
        }
        servers.add(server);
        saveServers();
        return server;
    }

    public void removeServer(UUID id) {
        servers.removeIf(s -> s.getId() != null && s.getId().equals(id));
        saveServers();
    }

    public void setActiveServer(UUID id) {
        if (id == null) {
            engineManager.switchToLocal();
        } else {
            Optional<ManagedServer> server = servers.stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst();
            
            if (server.isPresent()) {
                engineManager.switchToRemote(id, server.get().getUrl());
            } else {
                throw new IllegalArgumentException("Server not found in registry: " + id);
            }
        }
    }

    private void loadServers() {
        File file = new File(properties.getRegistry().getFilePath());
        if (file.exists()) {
            try {
                servers = objectMapper.readValue(file, new TypeReference<List<ManagedServer>>() {});
                log.info("Loaded {} servers from registry file", servers.size());
            } catch (IOException e) {
                log.error("Failed to load servers from registry", e);
            }
        }
    }

    private void saveServers() {
        File file = new File(properties.getRegistry().getFilePath());
        // Create parent directories if they don't exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        
        try {
            objectMapper.writeValue(file, servers);
            log.debug("Saved servers to registry file");
        } catch (IOException e) {
            log.error("Failed to save servers to registry", e);
        }
    }

    public UUID getActiveServerId() {
        return engineManager.getActiveServerId();
    }
}
