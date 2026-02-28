<div align="center">
  <img src="https://raw.githubusercontent.com/vinipx/wixy/main/documentation/static/img/logo.svg" width="120" alt="WIXY Hub Logo" />
  <h1>WIXY Hub</h1>
  <h3>The Command Center for your WireMock Fleet</h3>

[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://vinipx.github.io/wixy/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WireMock](https://img.shields.io/badge/WireMock-3.13.0-purple.svg)](https://wiremock.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

> **📚 Live Documentation: [https://vinipx.github.io/wixy/](https://vinipx.github.io/wixy/)**

WIXY Hub is a high-performance **orchestration plane** for service virtualization. It decouples the management of WireMock from the actual traffic processing, allowing you to control an entire fleet of local and remote mock engines from a single, unified interface.

---

## ✨ Core Pillars

### 🏗️ Fleet Orchestration
Manage multiple environments (Local, Docker, Staging, Cloud) from one place.
- **Central Registry:** A persistent database of all your mock servers.
- **Live Health Tracking:** Real-time connectivity status for every node in your fleet.
- **Engine Switching:** Instantly re-contextualize your entire dashboard to any engine with one click.

### 📺 Live Log Streaming (TUI)
A professional-grade **terminal console** built into your browser.
- **Xterm.js Integration:** Handles high-throughput traffic without browser lag.
- **Background Persistence:** Logs continue to be captured even when you switch tabs.
- **ANSI Support:** Color-coded HTTP methods, statuses, and timestamps for rapid diagnostics.
- **Pause & Search:** Suspend the stream to inspect payloads or search across 10,000+ lines of history.

### 🖥️ Unified Control Plane
A sleek React-based dashboard designed for high-velocity engineering.
- **Visual Stub Editor:** Full JSON editor with syntax highlighting and validation.
- **Runtime Proxying:** Dynamically toggle catch-all proxying to real upstream services.
- **One-Click Recording:** Convert live traffic into persistent stub mappings automatically.

### 🤖 AI-Native (Model Context Protocol)
WIXY Hub natively implements **MCP**, exposing your mock infrastructure as tools for AI agents like **Claude Desktop** or **Cursor**. Automate your testing through natural language.

---

## 🚀 Getting Started

### Start a 3-Node Fleet (Recommended for Dev)
Bootstraps the Hub plus two pre-configured remote engines to see orchestration in action:
```bash
./scripts/start-local.sh -3
```

### Start Standalone Hub
```bash
./gradlew bootRun
```

**Access the UI:** [http://localhost:8080](http://localhost:8080)

| Port | Purpose |
| :--- | :--- |
| **8080** | **WIXY Hub** (Management UI, API, Docs, MCP) |
| **9090** | **Local Engine** (Traffic Plane / Embedded WireMock) |

---

## 🎯 CI/CD Direct Targeting

Automated tests can bypass the "Active Engine" context and target specific servers atomically using the `X-Wixy-Target-Server` header.

```bash
# Target the 'payment-gateway' mock directly via the Hub
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "X-Wixy-Target-Server: <engine-uuid>" \
  -H "Content-Type: application/json" \
  -d '{ ...stub-definition... }'
```

---

## 🧪 Build & Verify

```bash
# Full Quality Gate (Backend + Frontend + Docs)
./gradlew check

# Generate Test Coverage
./gradlew jacocoTestReport
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
