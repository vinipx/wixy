---
sidebar_position: 2
title: Stub Management
---

# Stub Management

WIXY Hub provides a unified interface for creating, reading, updating, and deleting HTTP stubs. You can manage stubs through the **modern Web Dashboard**, the **REST API**, or using **AI agents via MCP**.

## How Stubs Work

A **stub mapping** defines how a WireMock engine responds to specific requests. It consists of:
1. **Request matcher** — Method, URL pattern, headers, and/or body.
2. **Response definition** — Status code, headers, and body to return.

Operations are performed on the Hub's current **Active Engine** unless specified otherwise via headers.

---

## 🖥️ Dashboard Management

The WIXY Hub Dashboard provides a user-friendly way to manage stubs without writing code.

### The Stub List
Navigate to the **Dashboard** tab to see all active mappings on the selected engine. 
- **Method Badges**: Quickly identify GET, POST, or other request types.
- **URL Tooltips**: Hover over URLs to see the full path matcher.
- **Priority Indicators**: See which stubs will match first.

### Create & Edit (Stub Editor)
Click **+ Create Stub** or the **Edit icon** on an existing row to open the Stub Editor.
- **JSON Editor**: A spacious, scrollable code editor for defining WireMock mappings.
- **Live Validation**: The Hub validates your JSON schema in real-time before saving.
- **Documentation Link**: Quick access to the official WireMock stubbing guide directly from the editor.

### View Details
Click the **External Link icon** to open a **Read-Only view** of any stub. This is perfect for inspecting complex mappings without risking accidental changes.

---

## 🛠️ API Management

For automation and power users, the Hub exposes a full REST API.

### Creating a Stub
```bash
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/users/1"
    },
    "response": {
      "status": 200,
      "jsonBody": { "id": 1, "name": "Jane Doe" },
      "headers": { "Content-Type": "application/json" }
    }
  }'
```

### Updating a Stub
```bash
curl -X PUT http://localhost:8080/wixy/admin/mappings/{uuid} \
  -H "Content-Type: application/json" \
  -d '{ ... updated json ... }'
```

### Deleting a Stub
```bash
# Delete specific
curl -X DELETE http://localhost:8080/wixy/admin/mappings/{uuid}

# Reset ALL stubs on active engine
curl -X POST http://localhost:8080/wixy/admin/mappings/reset
```

---

## 📁 Pre-Packaged Stubs

WIXY Hub supports pre-loading stubs from the filesystem. Place your `.json` files in:
`src/main/resources/wiremock/mappings/`

These stubs are automatically loaded into the **Local Embedded Engine** on startup.

:::tip Multi-Server Targeting
When using the API, you can target a specific remote server by adding the `X-Wixy-Target-Server: <server-id>` header to any of the calls above.
:::

## Error Handling

| Scenario | HTTP Status | Description |
|----------|-------------|-------------|
| Stub not found | `404` | The UUID does not exist on the target engine. |
| Invalid JSON | `400` | Malformed JSON or invalid WireMock schema. |
| Engine Offline | `500` | The target remote server is unreachable. |
