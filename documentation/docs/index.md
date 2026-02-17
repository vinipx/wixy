---
slug: /
sidebar_position: 1
title: Introduction
---

# WIXY Hub — WireMock Management Plane

**WIXY Hub** is a lightweight, configurable **control plane** for [WireMock](https://wiremock.org/). It provides a centralized management layer to manage, monitor, and control multiple WireMock instances—both embedded and remote—through a modern Web UI, REST API, or AI-native interface.

## Why WIXY Hub?

As microservice ecosystems grow, managing hundreds of stubs across local, staging, and QA environments becomes a bottleneck. WIXY Hub solves this by decoupling the **Management Plane** from the **Mocking Engine**:

- **Centralized Control** — Manage your entire mock fleet from a single professional dashboard.
- **Dual-Mode Architecture** — Use the embedded engine for local work or connect to remote servers for shared environments.
- **Dynamic Orchestration** — Switch contexts instantly or use headers to target specific servers per request.
- **AI-Native Operations** — Implement AI-driven testing by connecting agents directly to the Hub via MCP.

## Key Features

| Feature | Description |
|---------|-------------|
| **Modern Dashboard** | Full-featured React UI for stub management and engine control. |
| **Server Registry** | Register and track multiple local or remote WireMock environments. |
| **Direct Targeting** | Route management commands to specific servers via the `X-Wixy-Target-Server` header. |
| **Stub CRUD API** | Create, read, update, and delete HTTP stubs across any registered engine. |
| **Proxy & Record** | captured real traffic and auto-generate persistent stub mappings. |
| **MCP Integration** | Control the entire Hub using natural language via AI agents. |

## Hub Architecture at a Glance

WIXY Hub operates on two distinct planes:

```
┌──────────────────────────────────────────────────────────────┐
│                        WIXY HUB (Port 8080)                  │
│                                                              │
│  ┌──────────────┐      ┌──────────────┐      ┌────────────┐  │
│  │  Registry    │◀────▶│  Management  │◀────▶│  Web UI    │  │
│  │  (Servers)   │      │  Orchestrator│      │ (Dashboard)│  │
│  └──────────────┘      └───────┬──────┘      └────────────┘  │
│                                │                               │
│                    ┌───────────┴───────────┐                   │
│                    ▼                       ▼                   │
│           ┌────────────────┐      ┌────────────────┐           │
│           │ Local Engine   │      │ Remote Engine  │           │
│           │ (Port 9090)    │      │ (External API) │           │
│           └────────────────┘      └────────────────┘           │
└──────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Backend** | Java 21 + Spring Boot 3.4 | LTS / Modern |
| **Frontend** | React + Tailwind CSS + Lucide | Fast / Responsive |
| **Mock Engine** | WireMock | 3.13.x |
| **AI Protocol** | Spring AI (MCP) | 1.0.0-M8 |
| **Testing** | JUnit 5 + RestAssured | 186+ Tests |

## Quick Navigation

- **[Getting Started →](/docs/getting-started/quickstart)** — Launch the Hub in under a minute.
- **[Hub & Registry →](/docs/features/hub-registry)** — Learn how to manage multiple environments.
- **[Architecture →](/docs/architecture/overview)** — Deep dive into the Hub's internals.
- **[API Reference →](/docs/api/rest-endpoints)** — Registry and Engine API documentation.
- **[AI-Native (MCP) →](/docs/mcp/overview)** — Manage your fleet via AI agents.
