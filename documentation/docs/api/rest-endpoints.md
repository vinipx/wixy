---
sidebar_position: 1
title: REST API Reference
---

# REST API Reference

Complete reference for WIXY's Admin REST API. All endpoints are served on the **Spring Boot port** (default `8080`). Test traffic should be directed to the **WireMock port** (default `9090`).

:::tip
Interactive API documentation is available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) when WIXY is running.
:::

## Stub Management

### List All Stubs

```
GET /wixy/admin/mappings
```

**Response:** `200 OK`

```json
{
  "mappings": ["..."],
  "meta": {
    "total": 3
  }
}
```

---

### Create Stub

```
POST /wixy/admin/mappings
Content-Type: application/json
```

**Request Body:**

```json
{
  "request": {
    "method": "GET",
    "urlPath": "/api/resource"
  },
  "response": {
    "status": 200,
    "jsonBody": { "key": "value" },
    "headers": { "Content-Type": "application/json" }
  }
}
```

**Response:** `201 Created`

---

### Get Stub by ID

```
GET /wixy/admin/mappings/{uuid}
```

**Response:** `200 OK` or `404 Not Found`

---

### Update Stub

```
PUT /wixy/admin/mappings/{uuid}
Content-Type: application/json
```

**Request Body:** Same format as Create.

**Response:** `200 OK` or `404 Not Found`

---

### Delete Stub

```
DELETE /wixy/admin/mappings/{uuid}
```

**Response:** `204 No Content` or `404 Not Found`

---

### Reset All Stubs

```
POST /wixy/admin/mappings/reset
```

**Response:** `200 OK`

```json
{
  "status": "All mappings reset"
}
```

---

### Bulk Import Stubs

```
POST /wixy/admin/mappings/import
Content-Type: application/json
```

**Request Body:** WireMock stub mapping JSON.

**Response:** `200 OK`

```json
{
  "imported": 1
}
```

---

## Proxy Management

### Get Proxy Status

```
GET /wixy/admin/proxy
```

**Response:** `200 OK`

```json
{
  "enabled": false,
  "targetUrl": "",
  "record": false,
  "wiremockPort": 9090
}
```

---

### Enable Proxy

```
POST /wixy/admin/proxy/enable
Content-Type: application/json
```

**Request Body:**

```json
{
  "targetUrl": "https://api.example.com"
}
```

**Response:** `200 OK`

```json
{
  "status": "Proxy enabled",
  "targetUrl": "https://api.example.com"
}
```

---

### Disable Proxy

```
POST /wixy/admin/proxy/disable
```

**Response:** `200 OK`

```json
{
  "status": "Proxy disabled"
}
```

---

## Recording Management

### Start Recording

```
POST /wixy/admin/recordings/start
Content-Type: application/json
```

**Request Body (optional):**

```json
{
  "targetUrl": "https://api.example.com"
}
```

If `targetUrl` is omitted, falls back to `wixy.proxy.target-url`.

**Response:** `200 OK`

```json
{
  "status": "Recording started"
}
```

---

### Stop Recording

```
POST /wixy/admin/recordings/stop
```

**Response:** `200 OK`

```json
{
  "status": "Recording stopped",
  "capturedStubs": 5
}
```

---

### Get Recording Status

```
GET /wixy/admin/recordings/status
```

**Response:** `200 OK`

```json
{
  "status": "NeverStarted"
}
```

---

## Health & Monitoring

### Health Check

```
GET /actuator/health
```

**Response:** `200 OK`

```json
{
  "status": "UP",
  "components": {
    "wiremock": {
      "status": "UP",
      "details": {
        "port": 9090,
        "stubCount": 3
      }
    }
  }
}
```

---

## Error Response Format

All errors follow a consistent format:

```json
{
  "timestamp": "2025-01-15T10:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Stub mapping not found: a1b2c3d4-..."
}
```

| Status | Cause |
|--------|-------|
| `400` | Invalid stub JSON or malformed request |
| `401` | Missing or invalid `X-Wixy-Api-Key` header (when security enabled) |
| `404` | Stub UUID not found |
| `500` | Internal server error |

## Authentication

When security is enabled (`wixy.security.enabled=true`), include the API key header:

```
X-Wixy-Api-Key: your-api-key
```

Health and Swagger endpoints are exempt from authentication.
