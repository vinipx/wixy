---
sidebar_position: 1
title: Architecture Overview
---

# Architecture Overview

WIXY follows a **layered architecture** with clear separation of concerns. A Spring Boot application manages the lifecycle of an embedded WireMock server, exposing administrative controls through REST endpoints while WireMock handles the actual request stubbing and proxying.

## High-Level Architecture

```mermaid
graph TB
    subgraph WIXY["WIXY (Spring Boot Application)"]
        direction TB
        
        subgraph Controllers["REST Controllers (Port 8080)"]
            AC[AdminController<br/>/wixy/admin/mappings]
            PC[ProxyController<br/>/wixy/admin/proxy]
            RC[RecordingController<br/>/wixy/admin/recordings]
            HC[Actuator<br/>/actuator/health]
        end
        
        subgraph Services["Service Layer"]
            SS[StubService]
            PS[ProxyService]
            RS[RecordingService]
        end
        
        subgraph Config["Configuration"]
            WP[WixyProperties]
            SC[SecurityConfig]
            WC[WireMockConfig]
            HI[WireMockHealthIndicator]
        end
        
        subgraph Engine["Embedded WireMock (Port 9090)"]
            WM[WireMockServer]
            SM[Stub Mappings]
            PE[Proxy Engine]
            RR[Recorder]
        end
    end
    
    Client["Test Client"] -->|HTTP| Controllers
    TestTraffic["Test Traffic"] -->|HTTP| Engine
    Engine -->|Unmatched| Upstream["Upstream Service"]
    
    Controllers --> Services
    Services --> Engine
    Config --> Engine
    
    style WIXY fill:#141414,stroke:#06b6d4,color:#f4f4f5
    style Controllers fill:#18181b,stroke:#3f3f46,color:#f4f4f5
    style Services fill:#27272a,stroke:#3f3f46,color:#f4f4f5
    style Config fill:#3f3f46,stroke:#52525b,color:#f4f4f5
    style Engine fill:#52525b,stroke:#71717a,color:#f4f4f5
```

## Key Design Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| D1 | **Two ports**: Spring Boot on `8080`, WireMock on `9090` | Clear separation of management and traffic planes. Ports are independently configurable. |
| D2 | WireMock managed via a **Spring `@Bean`** with `@PostConstruct`/`@PreDestroy` | Lifecycle is tied to the Spring context — guarantees clean startup and graceful shutdown. |
| D3 | **Proxy/record mode** is opt-in via config | When `wixy.proxy.target-url` is set, unmatched requests proxy upstream. When empty, unmatched requests return 404. |
| D4 | Stubs from **classpath AND runtime API** | Pre-packaged stubs in `classpath:/wiremock/mappings/` load on startup; dynamic stubs created via Admin API at runtime. |
| D5 | **No authentication by default** | Test tools should be frictionless locally but lockable in shared environments via optional API-key. |
| D6 | **Configuration externalised** via profiles + env vars | 12-factor compliant. Easy to configure in any runtime (local, Docker, K8s, cloud). |

## Component Interaction Flow

### Stub Creation Flow

```mermaid
sequenceDiagram
    participant C as Test Client
    participant AC as AdminController
    participant SS as StubService
    participant WM as WireMockServer
    
    C->>AC: POST /wixy/admin/mappings
    AC->>SS: create(json)
    SS->>WM: addStubMapping(mapping)
    WM-->>SS: StubMapping (with UUID)
    SS-->>AC: Created stub
    AC-->>C: 201 Created + stub JSON
```

### Request Matching Flow

```mermaid
sequenceDiagram
    participant T as Test Traffic
    participant WM as WireMockServer
    participant SM as Stub Mappings
    participant PE as Proxy Engine
    participant US as Upstream Service
    
    T->>WM: GET /api/resource
    WM->>SM: Find matching stub
    
    alt Stub Found
        SM-->>WM: Matched response
        WM-->>T: Stubbed response (200)
    else No Match + Proxy Enabled
        WM->>PE: Forward to upstream
        PE->>US: GET /api/resource
        US-->>PE: Real response
        PE-->>WM: Proxied response
        WM-->>T: Upstream response
    else No Match + Proxy Disabled
        WM-->>T: 404 Not Found
    end
```

## Package Structure

```
io.github.vinipx.wixy/
├── WixyApplication.java              # Spring Boot entry point
├── config/
│   ├── WireMockConfig.java            # WireMock bean + lifecycle
│   ├── WixyProperties.java            # @ConfigurationProperties
│   ├── SecurityConfig.java            # Optional API-key filter
│   └── WireMockHealthIndicator.java   # Custom health indicator
├── controller/
│   ├── AdminController.java           # Stub CRUD REST API
│   ├── ProxyController.java           # Proxy config toggles
│   └── RecordingController.java       # Record/stop/status
├── service/
│   ├── StubService.java               # Stub business logic
│   ├── ProxyService.java              # Proxy management logic
│   └── RecordingService.java          # Recording lifecycle
└── exception/
    ├── WixyException.java             # Base exception
    ├── StubNotFoundException.java     # 404 for missing stubs
    ├── InvalidStubDefinitionException.java  # 400 for bad JSON
    └── GlobalExceptionHandler.java    # @ControllerAdvice
```

## Resource Structure

```
src/main/resources/
├── application.yml              # Default configuration
├── application-local.yml        # Local dev overrides
├── application-docker.yml       # Docker overrides
├── application-cloud.yml        # Cloud overrides (security ON)
└── wiremock/
    ├── mappings/                 # Pre-packaged stub JSON files
    │   └── sample-stub.json
    └── __files/                  # Response body files
        └── sample-response.json
```

## Thread Model

WIXY runs two independent server threads:

1. **Spring Boot (Tomcat)** — Handles Admin API requests on port `8080`
2. **WireMock (Jetty)** — Handles stub/proxy traffic on port `9090`

Both servers share the same JVM process, enabling direct in-process communication between the Spring service layer and WireMock's internal APIs without network overhead.

:::info
For detailed component specifications including every class and method, see the [Layers documentation](/docs/architecture/layers).
:::
