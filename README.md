# WIXY Hub — Enterprise WireMock Orchestrator

[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://vinipx.github.io/wixy/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WireMock](https://img.shields.io/badge/WireMock-3.13.0-purple.svg)](https://wiremock.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **The Central Management Plane for your WireMock Fleet.**

WIXY Hub is a professional, Spring Boot-based orchestrator designed to simplify service virtualisation. It allows developers and QA teams to control, monitor, and manage an entire fleet of WireMock engines—both local and remote—from a single, modern dashboard.

---

## 🚀 Quick Start

### 1. Launch the Hub
Requires Java 21+.
```bash
./gradlew bootRun
```

### 2. Access the Dashboard
Open your browser at **[http://localhost:8080](http://localhost:8080)**.

**Port Mapping:**
- **8080**: WIXY Hub (Management UI, Admin API, Self-hosted Docs, MCP)
- **9090**: Traffic Plane (Default Embedded WireMock Engine)

---

## ✨ Key Capabilities

- 🏢 **Fleet Orchestration** — Manage multiple WireMock instances across your infrastructure from one central Hub.
- 🌐 **Remote Management** — Register and control external servers (Staging, QA, Cloud) as easily as local ones.
- 🖥️ **Modern Dashboard** — A high-performance React UI for visual stub management, recording, and real-time health monitoring.
- 🛠️ **Full Stub CRUD** — Create, edit, and update mappings using a powerful JSON editor with live validation.
- 🎯 **Direct Targeting** — Route requests to specific engines atomically using the `X-Wixy-Target-Server` header.
- 🕵️ **Traffic Recorder** — Capture live traffic on any engine and automatically transform it into persistent stub mappings.
- 🤖 **AI-Native (MCP)** — Native support for Model Context Protocol. Manage your fleet using natural language via AI agents.

---

## 🎯 Direct Targeting (Power Users)

WIXY Hub supports per-request orchestration. You can target any registered server without changing the global "Active Engine" context:

```bash
# Register a stub on a specific remote engine via the central Hub
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "X-Wixy-Target-Server: <server-uuid>" \
  -H "Content-Type: application/json" \
  -d '{ ... stub json ... }'
```

---

## 🤖 Model Context Protocol (MCP)

WIXY Hub is AI-native. It exposes its orchestration tools via SSE, allowing AI agents (like Claude or Cursor) to adapt your test environment on the fly.

**MCP Endpoint:** `http://localhost:8080/wixy/mcp`

> *"Claude, connect to the inventory-mock and simulate a 503 error for all POST /orders requests."*

---

## ⚙️ Configuration

| Environment Variable | Default | Description |
|:--- |:--- |:--- |
| `WIXY_REGISTRY_FILE_PATH` | `~/.wixy/servers.json` | Path to the persistent server registry |
| `WIXY_UI_ENABLED` | `true` | Enable/Disable the management dashboard |
| `WIXY_WIREMOCK_PORT` | `9090` | Port for the embedded WireMock engine |
| `WIXY_PROXY_TARGET_URL` | `""` | Default upstream URL for proxy/recording |
| `WIXY_SECURITY_ENABLED` | `false` | Enable API-key header protection |

---

## 🧪 Build & Verify

```bash
# Build and run all quality checks (Unit + Integration + Frontend + Docs)
./gradlew check

# Generate JaCoCo coverage report
./gradlew jacocoTestReport
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
