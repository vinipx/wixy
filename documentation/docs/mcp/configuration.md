---
sidebar_position: 2
title: Configuration
---

# MCP Configuration

The WIXY MCP server is enabled by default in most profiles. It is built on top of the Spring AI MCP implementation and uses **SSE (Server-Sent Events)** as the primary transport.

## Core Properties

Configuration is managed via Spring Boot properties in `application.yml` or through environment variables.

| Property | Environment Variable | Default | Description |
| :--- | :--- | :--- | :--- |
| `spring.ai.mcp.server.enabled` | `SPRING_AI_MCP_SERVER_ENABLED` | `true` | Toggle the MCP server. |
| `spring.ai.mcp.server.name` | `SPRING_AI_MCP_SERVER_NAME` | `Wixy-MCP-Server` | Name reported to clients. |
| `spring.ai.mcp.server.transport` | `SPRING_AI_MCP_SERVER_TRANSPORT` | `sse` | Transport mechanism (SSE or STDIO). |
| `spring.ai.mcp.server.path` | `SPRING_AI_MCP_SERVER_PATH` | `/wixy/mcp` | The HTTP path for SSE handshake. |

## Example `application.yml`

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "Wixy-MCP-Server"
        version: "1.0.0"
        transport: sse
        path: /wixy/mcp
```

## Security

When `wixy.security.enabled=true`, the MCP endpoint is protected by the same API-key filter as the REST API.

- **Header:** `X-Wixy-Api-Key`
- **Value:** Your configured secret key.

:::warning
Some MCP clients (like early versions of Claude Desktop) may have limited support for custom headers in SSE. Ensure your client supports headers if you enable security.
:::

## Disabling MCP

If you want to run WIXY without the AI interface (e.g., in a performance-critical production environment), you can disable it entirely:

```bash
export SPRING_AI_MCP_SERVER_ENABLED=false
java -jar wixy.jar
```
