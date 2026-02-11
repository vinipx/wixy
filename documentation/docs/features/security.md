---
sidebar_position: 4
title: Security
---

# Security

WIXY provides optional API-key authentication for shared and cloud environments. When enabled, all requests must include a valid `X-Wixy-Api-Key` header.

## Configuration

### Enable Security

```yaml title="application.yml"
wixy:
  security:
    enabled: true
    api-key: "your-secret-api-key-here"
```

### Via Environment Variables

```bash
export WIXY_SECURITY_ENABLED=true
export WIXY_SECURITY_API_KEY=my-secret-key-2025
java -jar wixy.jar
```

## How It Works

When `wixy.security.enabled=true`, a `OncePerRequestFilter` is registered at the highest precedence:

1. **Check path** — If the path is in the allow-list, the request passes through without authentication
2. **Check header** — If `X-Wixy-Api-Key` matches the configured value, the request proceeds
3. **Reject** — Otherwise, returns `401 Unauthorized`

## Allow-Listed Paths

These paths are always accessible without authentication:

| Path | Purpose |
|------|---------|
| `/actuator/health` | Health checks (load balancers, K8s probes) |
| `/actuator/info` | Application info |
| `/swagger-ui.html` | OpenAPI documentation |
| `/v3/api-docs` | OpenAPI specification |

## Usage

### Authenticated Request

```bash
curl -X GET http://localhost:8080/wixy/admin/mappings \
  -H "X-Wixy-Api-Key: my-secret-key-2025"
```

### Unauthenticated Request

```bash
curl -X GET http://localhost:8080/wixy/admin/mappings
```

**Response (401):**

```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid X-Wixy-Api-Key header"
}
```

## Cloud Profile Default

The `cloud` profile enables security by default:

```yaml title="application-cloud.yml"
wixy:
  security:
    enabled: ${WIXY_SECURITY_ENABLED:true}
    api-key: ${WIXY_SECURITY_API_KEY:}
```

:::warning
The API key is transmitted in plain text. Always use **HTTPS** (TLS termination at load balancer or ingress) in shared/cloud environments.
:::

## Best Practices

| Practice | Description |
|----------|-------------|
| **Use environment variables** | Never commit API keys to source control |
| **Enforce HTTPS** | Terminate TLS at the load balancer or ingress controller |
| **Rotate keys regularly** | Update `WIXY_SECURITY_API_KEY` on a regular schedule |
| **Disable locally** | Keep `wixy.security.enabled=false` for local development |
| **Use K8s secrets** | Store keys in Kubernetes Secrets, not ConfigMaps |
