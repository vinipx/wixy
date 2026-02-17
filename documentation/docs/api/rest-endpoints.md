---
sidebar_position: 1
title: REST API Reference
---

# REST API Reference

Complete reference for WIXY Hub's Admin REST API. All endpoints are served on the **Hub port** (default `8080`).

:::tip Direct Engine Targeting
All Management APIs support the `X-Wixy-Target-Server` header. Include it to route a request to a specific engine without changing the Hub's global active context.
- `local`: The embedded Port 9090 server.
- `<UUID>`: A registered remote server ID.
:::

## Registry Management

### List Managed Servers
```
GET /wixy/admin/registry/servers
```

---

### Add Remote Server
```
POST /wixy/admin/registry/servers
Content-Type: application/json
```
**Body:**
```json
{
  "name": "Staging API",
  "url": "http://staging-wiremock:8080"
}
```

---

### Set Active Engine
```
POST /wixy/admin/registry/active
Content-Type: application/json
```
**Body:**
```json
{
  "id": "a1b2c3d4-..."
}
```
Use `"id": "local"` to switch back to the embedded engine.

---

## Stub Management
*Operations are performed on the current **Active Engine** unless a target header is provided.*

### List All Stubs
```
GET /wixy/admin/mappings
```

---

### Create Stub
```
POST /wixy/admin/mappings
Content-Type: application/json
```

---

## Proxy Management

### Get Proxy Status
```
GET /wixy/admin/proxy
```

### Enable Proxy
```
POST /wixy/admin/proxy/enable
Content-Type: application/json
```
**Body:**
```json
{
  "targetUrl": "https://api.example.com"
}
```

---

## Recording Management

### Start Recording
```
POST /wixy/admin/recordings/start
Content-Type: application/json
```

### Stop Recording
```
POST /wixy/admin/recordings/stop
```

---

## Health & Monitoring

### Hub Health
```
GET /actuator/health
```
Includes the health status of the **current active engine** under the `wiremock` component.
