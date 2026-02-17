<div align="center">
  <img src="https://raw.githubusercontent.com/vinipx/wixy/main/documentation/static/img/logo.svg" width="120" alt="WIXY Hub Logo" />
  <h1>WIXY Hub</h1>
  <h3>Enterprise WireMock Orchestrator</h3>

[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://vinipx.github.io/wixy/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WireMock](https://img.shields.io/badge/WireMock-3.13.0-purple.svg)](https://wiremock.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

> **📚 Live Documentation: [https://vinipx.github.io/wixy/](https://vinipx.github.io/wixy/)**

WIXY Hub is a professional, Spring Boot-based orchestrator designed to simplify service virtualisation. It allows developers and QA teams to **control, monitor, and manage an entire fleet of WireMock engines**—both local and remote—from a single, modern dashboard.

Whether you are running a single mock for local development or orchestrating a complex microservices test environment, WIXY Hub provides the central management plane you need.

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

## 🏢 Fleet Orchestration & Remote Management

The core power of WIXY Hub lies in its ability to manage **multiple WireMock instances** transparently.

- **Unified Registry**: Maintain a persistent list of all your mock servers (Staging, QA, Cloud) in one place.
- **Context Switching**: Instantly switch the Hub's active context to any registered server. All UI actions (creating stubs, recording) are automatically routed to the active engine.
- **Health Monitoring**: The Hub proactively monitors the reachability of all remote engines, giving you real-time visibility into your test infrastructure health.

**Use Case:** A QA engineer can fix a broken stub on the `staging-payment-mock` directly from their local WIXY Hub dashboard without needing SSH access or complex configuration changes.

---

## 🖥️ Modern Management Dashboard

Forget raw cURL commands. WIXY Hub provides a sleek, high-performance React UI.

- **Visual Stub Editor**: Create and edit complex JSON mappings with syntax highlighting and live validation.
- **Traffic Recorder**: Capture live requests passing through any engine and automatically convert them into persistent stubs with one click.
- **Live Proxy Control**: Toggle proxy settings and update upstream target URLs at runtime for any engine in your fleet.
- **Stub Search & Filter**: Quickly find specific mappings across hundreds of stubs using method, URL, and status filters.

---

## 🎯 Direct Targeting (CI/CD Automation)

For automated pipelines, WIXY Hub supports **atomic, per-request orchestration**. You can target any registered server without changing the global "Active Engine" context, enabling high-concurrency testing.

Simply add the `X-Wixy-Target-Server` header to any Admin API request:

```bash
# Register a stub on the 'inventory-mock' specifically
curl -X POST http://localhost:8080/wixy/admin/mappings \
  -H "X-Wixy-Target-Server: inventory-mock-id" \
  -H "Content-Type: application/json" \
  -d '{
    "request": { "method": "GET", "url": "/api/stock/123" },
    "response": { "status": 200, "jsonBody": { "count": 50 } }
  }'
```

**Use Case:** A CI pipeline running parallel integration tests can dynamically configure the `payment-mock` for Scenario A and the `inventory-mock` for Scenario B simultaneously via the same Hub instance.

---

## 🤖 AI-Native Integration (MCP)

WIXY Hub is built for the AI era. It natively implements the **Model Context Protocol (MCP)**, exposing its entire orchestration capability as tools for AI agents.

Connect **Claude Desktop**, **Cursor**, or any MCP-compliant client to:
- *"Connect to the QA mock server."*
- *"Analyze the last 10 requests and create a stub for the failed one."*
- *"Simulate high latency on the checkout endpoint."*

**MCP Endpoint:** `http://localhost:8080/wixy/mcp`

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

The project enforces strict quality gates. The build process verifies backend logic, frontend assets, and documentation integrity.

```bash
# Build and run all quality checks (Unit + Integration + Frontend + Docs)
./gradlew check

# Generate JaCoCo coverage report
./gradlew jacocoTestReport
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
