# Wixy — WireMock Proxy Server on Spring Boot

[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://vinipx.github.io/wixy/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WireMock](https://img.shields.io/badge/WireMock-3.13.0-purple.svg)](https://wiremock.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-MCP-blue.svg)](https://spring.io/projects/spring-ai)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **See Docs -> [https://vinipx.github.io/wixy/](https://vinipx.github.io/wixy/)**

Wixy is a lightweight, developer-first test proxy service that embeds [WireMock](https://wiremock.org/) inside a Spring Boot application. It bridges the gap between static stubbing and dynamic integration testing by providing a clean management API alongside an AI-native interface.

---

## 🚀 Quick Start

### Local Development
Requires Java 21+.
```bash
./scripts/start-local.sh
```

### Docker
```bash
./scripts/start-docker.sh
```

**Port Mapping:**
- **8080**: Management Plane (Admin API, Actuator, Swagger, MCP)
- **9090**: Traffic Plane (WireMock Stub Server)

---

## ✨ Features

- 🛠️ **Stub Management** — Full CRUD REST API to manage HTTP stubs at runtime.
- 🔄 **Proxy Mode** — Transparently forward unmatched requests to any upstream service.
- 🕵️ **Record & Playback** — Automatically capture real traffic and save it as persistent stub mappings.
- 🤖 **MCP Integration** — Native support for Model Context Protocol to control your proxy via AI agents.
- 🛡️ **Security** — Optional API-key header protection for management endpoints.
- 📊 **Observability** — Built-in Spring Boot Actuator with custom WireMock health indicators.
- 📖 **OpenAPI Documentation** — Interactive Swagger UI available at `/swagger-ui.html`.

---

## 🤖 Model Context Protocol (MCP)

Wixy is AI-native. By integrating **Spring AI**, it exposes an MCP server over SSE (Server-Sent Events), allowing AI agents (like Claude Desktop, Cursor, or local LLMs) to manage your test environment through natural language.

**MCP Endpoint:** `http://localhost:8080/wixy/mcp`

### Use Cases:
- **Generative Mocking**: *"Create a 500 error stub for the /checkout endpoint."*
- **Intelligent Debugging**: *"List all stubs and tell me why the last request to /users failed."*
- **Automated Recording**: *"Start recording from staging, I'm going to run the login test suite now."*

*Check the [MCP Guide](https://vinipx.github.io/wixy/docs/features/mcp-integration) for more details.*

---

## ⚙️ Configuration

Wixy uses Spring Boot profiles (`local`, `docker`, `cloud`) and can be fully configured via environment variables.

| Environment Variable | Default | Description |
|:--- |:--- |:--- |
| `WIXY_WIREMOCK_PORT` | `9090` | Port for the WireMock stub server |
| `WIXY_PROXY_ENABLED` | `false` | Enable/Disable upstream proxying |
| `WIXY_PROXY_TARGET_URL` | `""` | Target URL for proxy/recording |
| `WIXY_SECURITY_ENABLED` | `false` | Enable X-Wixy-Api-Key validation |
| `WIXY_SECURITY_API_KEY` | `""` | The required API-key value |
| `SPRING_AI_MCP_SERVER_ENABLED` | `true` | Toggle the MCP server |

---

## 🧪 Build & Test

Wixy maintains high quality through automated testing and coverage reports.

```bash
# Full check (unit + integration)
./gradlew check

# Run application
./gradlew bootRun

# Generate JaCoCo coverage report
./gradlew jacocoTestReport
```

---

## 🗺️ Architecture

Wixy follows a clean layered architecture, separating the **Management Plane** (Spring Boot) from the **Traffic Plane** (Embedded WireMock).

- **Controllers**: REST and MCP interfaces.
- **Services**: Business logic for stub lifecycle and server control.
- **Engine**: Embedded WireMock server running in-process for maximum performance.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
