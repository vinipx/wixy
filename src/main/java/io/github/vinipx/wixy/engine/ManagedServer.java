package io.github.vinipx.wixy.engine;

import java.util.UUID;

/**
 * Represents a WireMock server managed by Wixy.
 */
public class ManagedServer {
    private UUID id;
    private String name;
    private String url;
    private ServerType type;

    public ManagedServer() {}

    public ManagedServer(UUID id, String name, String url, ServerType type) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public ServerType getType() { return type; }
    public void setType(ServerType type) { this.type = type; }

    public enum ServerType {
        INTERNAL, REMOTE
    }
}
