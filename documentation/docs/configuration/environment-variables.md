---
sidebar_position: 2
title: Environment Variables
---

# Environment Variables

WIXY Hub follows [12-Factor App](https://12factor.net/) principles. Every configuration property can be overridden via environment variables.

## Complete Reference

| Environment Variable | Maps To | Default | Description |
|---------------------|---------|---------|-------------|
| `SERVER_PORT` | `server.port` | `8080` | Hub HTTP port (UI/API) |
| `WIXY_REGISTRY_FILE_PATH` | `wixy.registry.file-path` | `~/.wixy/servers.json` | Path to persistent registry file |
| `WIXY_UI_ENABLED` | `wixy.ui.enabled` | `true` | Toggle management dashboard |
| `WIXY_WIREMOCK_PORT` | `wixy.wiremock.port` | `9090` | Embedded engine port |
| `WIXY_WIREMOCK_VERBOSE` | `wixy.wiremock.verbose` | `true` | Verbose WireMock logging |
| `WIXY_WIREMOCK_ROOT_DIR` | `wixy.wiremock.root-dir` | `classpath:/wiremock` | WireMock mappings directory |
| `WIXY_PROXY_ENABLED` | `wixy.proxy.enabled` | `false` | Enable proxy forwarding |
| `WIXY_PROXY_TARGET_URL` | `wixy.proxy.target-url` | (empty) | Upstream URL for proxy/recording |
| `WIXY_PROXY_RECORD` | `wixy.proxy.record` | `false` | Enable auto-recording on startup |
| `WIXY_SECURITY_ENABLED` | `wixy.security.enabled` | `false` | Enable API-key authentication |
| `WIXY_SECURITY_API_KEY` | `wixy.security.api-key` | (empty) | Required API-key value |
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active` | (none) | Active Spring profile(s) |

## Usage Examples

### Custom Registry Path

```bash
docker run -d \
  -p 8080:8080 \
  -e WIXY_REGISTRY_FILE_PATH=/data/servers.json \
  wixy:latest
```

### Headless Hub (API Only)

```bash
java -jar wixy.jar --wixy.ui.enabled=false
```

## Naming Convention

Spring Boot converts environment variables to properties using relaxed binding:

| Environment Variable | Property |
|---------------------|----------|
| `WIXY_REGISTRY_FILE_PATH` | `wixy.registry.file-path` |
| `WIXY_UI_ENABLED` | `wixy.ui.enabled` |

**Rule:** Replace dots with underscores and use UPPER_CASE.
