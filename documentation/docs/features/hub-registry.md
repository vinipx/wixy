---
sidebar_position: 1
title: Hub & Registry
---

# WIXY Hub & Registry

WIXY Hub transforms a single proxy utility into a powerful **control plane** for your entire WireMock infrastructure. It allows you to register multiple environments and orchestrate them from a single interface.

## Core Concepts

### The Registry
The Registry is a persistent list of "Managed Servers". Each entry consists of:
- **Name**: A human-readable identifier (e.g., "Staging API").
- **URL**: The base URL of the WireMock Admin API.
- **Type**: Either `INTERNAL` (the embedded engine) or `REMOTE`.

The registry is stored in a JSON file (default: `~/.wixy/servers.json`) and survives application restarts.

### The Active Engine
At any given time, the Hub has one **Active Engine**. 
- All standard REST API calls to `/wixy/admin/mappings` are routed to this engine.
- All MCP tool calls from AI agents are routed to this engine.
- The **Management UI** focuses on this engine for live monitoring and control.

## Direct Targeting (Advanced)

WIXY Hub supports **Direct Targeting**, allowing you to bypass the current global context for a single request. This is ideal for automated scripts or cross-environment synchronization.

### Using the Header
Include the `X-Wixy-Target-Server` header in any management request:

```bash
# Get mappings from a specific remote server (using its UUID)
curl http://localhost:8080/wixy/admin/mappings 
  -H "X-Wixy-Target-Server: a1b2c3d4-e5f6..."

# Create a stub on the LOCAL engine regardless of Hub context
curl -X POST http://localhost:8080/wixy/admin/mappings 
  -H "X-Wixy-Target-Server: local" 
  -d '{...}'
```

| Header Value | Routing Behavior |
|--------------|------------------|
| `local`      | Routes to the embedded Port 9090 engine. |
| `<UUID>`     | Routes to the registered remote server with that ID. |
| (missing)    | Routes to the current **Active Engine**. |

## Dashboard Operations

The modern Web UI provides two primary views for Hub management:

### 1. Server Registry View
- **Status Monitoring**: Live "Pulse" indicators for every registered server.
- **Context Switching**: One-click "Switch To" button to change the Hub's focus.
- **Management**: Add, edit, or remove remote servers from your fleet.

### 2. Engine Control View
- **Stub Manager**: A real-time data grid of all active mappings on the target server.
- **Live Controls**: Toggle Proxy Mode or Traffic Recording on the fly.
- **Engine Metrics**: Quick visibility into port status and stub counts.

## Configuration

You can customize the Registry behavior in `application.yml`:

```yaml
wixy:
  registry:
    persistence: file # Currently supports 'file'
    file-path: ${user.home}/.wixy/servers.json
  ui:
    enabled: true # Toggle the dashboard
```
