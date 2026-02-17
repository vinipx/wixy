# Wixy Hub — Multi-Engine WireMock Management Plane

[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://vinipx.github.io/wixy/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WireMock](https://img.shields.io/badge/WireMock-3.13.0-purple.svg)](https://wiremock.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **See Docs -> [https://vinipx.github.io/wixy/](https://vinipx.github.io/wixy/)**

WIXY Hub is a professional management plane for [WireMock](https://wiremock.org/). It allows developers and QA teams to control, monitor, and orchestrate multiple WireMock instances—both local and remote—from a single modern dashboard.

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
- **8080**: WIXY Hub (Management UI, Admin API, MCP)
- **9090**: Traffic Plane (Embedded WireMock Engine)

---

## ✨ Key Features

- 🖥️ **Centralized Dashboard** — A modern React UI to manage your entire mock server fleet.
- 🌐 **Remote Management** — Connect and control external WireMock servers as if they were local.
- 🎯 **Direct Targeting** — Route requests to specific servers using the `X-Wixy-Target-Server` header.
- 🛠️ **Stub Orchestration** — CRUD operations, bulk imports, and real-time mapping visualization.
- 🔄 **Live Proxy Control** — Toggle proxy modes and upstream targets at runtime across any engine.
- 🕵️ **Traffic Recorder** — Capture traffic from any registered server and save it as persistent stubs.
- 🤖 **AI-Native (MCP)** — Control the Hub using natural language via Claude, Cursor, or specialized agents.

---

## 🎯 Direct Targeting (API Power Users)

Wixy Hub supports per-request engine overrides. You can target any registered server without changing the global context:

```bash
# Get mappings from a specific remote server via the Hub
curl http://localhost:8080/wixy/admin/mappings \
  -H "X-Wixy-Target-Server: <server-uuid>"
```

---

## 🤖 Model Context Protocol (MCP)

WIXY Hub is AI-native. By integrating **Spring AI**, it exposes an MCP server over SSE, allowing AI agents to manage your entire test fleet through natural language.

**MCP Endpoint:** `http://localhost:8080/wixy/mcp`

### Example Agent Commands:
- *"Claude, connect to the staging mock and list all stubs."*
- *"Make the 'Auth Service' on Remote-A return a 401 for /login."*
- *"Record the next 5 requests on the local engine and save them."*

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

## 🧪 Build & Test

```bash
# Full build + quality check (unit + integration + frontend)
./gradlew check

# Generate coverage report
./gradlew jacocoTestReport
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
