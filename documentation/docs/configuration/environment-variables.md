---
sidebar_position: 2
title: Environment Variables
---

# Environment Variables

WIXY follows [12-Factor App](https://12factor.net/) principles. Every configuration property can be overridden via environment variables.

## Complete Reference

| Environment Variable | Maps To | Default | Description |
|---------------------|---------|---------|-------------|
| `SERVER_PORT` | `server.port` | `8080` | Spring Boot HTTP port |
| `WIXY_WIREMOCK_PORT` | `wixy.wiremock.port` | `9090` | WireMock stub server port |
| `WIXY_WIREMOCK_VERBOSE` | `wixy.wiremock.verbose` | `true` | Verbose WireMock logging |
| `WIXY_WIREMOCK_ROOT_DIR` | `wixy.wiremock.root-dir` | `classpath:/wiremock` | WireMock mappings directory |
| `WIXY_PROXY_ENABLED` | `wixy.proxy.enabled` | `false` | Enable proxy forwarding |
| `WIXY_PROXY_TARGET_URL` | `wixy.proxy.target-url` | (empty) | Upstream URL for proxy/recording |
| `WIXY_PROXY_RECORD` | `wixy.proxy.record` | `false` | Enable auto-recording on startup |
| `WIXY_SECURITY_ENABLED` | `wixy.security.enabled` | `false` | Enable API-key authentication |
| `WIXY_SECURITY_API_KEY` | `wixy.security.api-key` | (empty) | Required API-key value |
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active` | (none) | Active Spring profile(s) |

## Usage Examples

### Local Development

```bash
# All defaults — just run
java -jar wixy.jar
```

### Docker with Proxy

```bash
docker run -d \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e WIXY_PROXY_ENABLED=true \
  -e WIXY_PROXY_TARGET_URL=https://api.example.com \
  wixy:latest
```

### Cloud with Security

```bash
docker run -d \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e WIXY_SECURITY_ENABLED=true \
  -e WIXY_SECURITY_API_KEY=prod-secret-key-2025 \
  -e WIXY_PROXY_ENABLED=true \
  -e WIXY_PROXY_TARGET_URL=https://production-api.example.com \
  wixy:latest
```

### Recording Mode

```bash
export WIXY_PROXY_ENABLED=true
export WIXY_PROXY_TARGET_URL=https://api.example.com
export WIXY_PROXY_RECORD=true
java -jar wixy.jar --spring.profiles.active=local
```

### Custom Ports

```bash
export SERVER_PORT=3000
export WIXY_WIREMOCK_PORT=3001
java -jar wixy.jar
```

## Naming Convention

Spring Boot converts environment variables to properties using relaxed binding:

| Environment Variable | Property |
|---------------------|----------|
| `WIXY_WIREMOCK_PORT` | `wixy.wiremock.port` |
| `WIXY_PROXY_TARGET_URL` | `wixy.proxy.target-url` |
| `WIXY_SECURITY_API_KEY` | `wixy.security.api-key` |

**Rule:** Replace dots with underscores and use UPPER_CASE.

## Validation

Properties are validated at startup using Jakarta Bean Validation:

| Property | Constraint | Error on Violation |
|----------|-----------|-------------------|
| `wixy.wiremock.port` | `@Min(0) @Max(65535)` | Application fails to start |
| `wixy.wiremock` | `@NotNull` | Application fails to start |
| `wixy.proxy` | `@NotNull` | Application fails to start |
| `wixy.security` | `@NotNull` | Application fails to start |
