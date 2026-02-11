---
sidebar_position: 6
title: MCP Integration
---

# Model Context Protocol (MCP) Integration

Wixy supports the **Model Context Protocol (MCP)**, allowing AI models and agents to interact with the test proxy using natural language. This enables powerful use cases like generative mocking, automated traffic analysis, and interactive debugging.

## Architecture

Wixy uses **Spring AI** to host an MCP server directly within the application. The server is exposed via **Server-Sent Events (SSE)** on the management port.

- **MCP Endpoint:** `http://localhost:8080/wixy/mcp`
- **Transport:** SSE (Server-Sent Events)

## Available Tools

The following tools are exposed to the AI model:

| Tool Name | Description |
| :--- | :--- |
| `listStubs` | Lists all active WireMock stub mappings. |
| `createStub` | Creates a new stub mapping from a JSON definition. |
| `deleteStub` | Deletes a stub mapping by its UUID. |
| `enableProxy` | Enables proxying for unmatched requests to a target URL. |
| `disableProxy` | Disables proxy mode. |
| `startRecording` | Starts recording traffic to create stubs automatically. |
| `stopRecording` | Stops recording and returns the number of captured stubs. |

## Connecting a Client

### Claude Desktop
To use Wixy with Claude Desktop, add the following to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "wixy": {
      "command": "curl",
      "args": ["-s", "http://localhost:8080/wixy/mcp"]
    }
  }
}
```
*Note: Since Wixy uses SSE over HTTP, the client simply needs to connect to the URL.*

### MCP Inspector
You can verify the integration using the MCP Inspector:

```bash
npx @modelcontextprotocol/inspector http://localhost:8080/wixy/mcp
```

## Security

If `wixy.security.enabled` is set to `true`, the MCP endpoints will require the `X-Wixy-Api-Key` header. Ensure your MCP client is configured to send this header if security is enabled.

## For Developers: Accelerating the Development Cycle

Developers can use the MCP integration to quickly scaffold environment state without leaving their IDE (if using an MCP-compatible IDE like Cursor or Windsurf) or their AI chat interface.

### 1. Rapid Mock Generation
Instead of manually crafting complex WireMock JSON, describe the response you need.
*   **Prompt:** *"Create a stub for a GET request to `/api/v2/products`. It should return a list of 3 products with random IDs and names. One product should have a `discount` field set to 50%."*
*   **Outcome:** The AI generates the valid WireMock JSON and calls `createStub` automatically.

### 2. Debugging Integration Failures
When a frontend or service integration fails, ask the AI to inspect what's happening.
*   **Prompt:** *"List the current stubs. Is there any mapping for `/api/v2/products`? If not, create one that returns a 200 OK."*
*   **Outcome:** The AI checks the current state and fixes the missing mock immediately.

## For QA Engineers: Intelligent Test Automation

QA engineers can leverage MCP to manage test data and environments dynamically during manual or automated testing sessions.

### 1. Zero-Config Traffic Capture
Record real-world scenarios to create reproducible test cases.
*   **User Action:** *"Start recording from the staging environment."*
*   **Manual Step:** Perform the actions in the UI.
*   **User Action:** *"Stop recording and show me how many stubs were captured."*
*   **Outcome:** The AI captures the traffic, sanitizes the JSON (if the model is instructed to), and saves them as permanent stubs in Wixy.

### 2. Chaos Engineering & Fault Injection
Easily simulate system failures to test resilience.
*   **Prompt:** *"I want to test how the UI handles a slow backend. Create a stub for `/api/checkout` with a 5-second fixed delay and a 503 status code."*
*   **Outcome:** The AI configures the failure state, allowing the QA engineer to verify error handling without backend changes.

### 3. Environment Synchronization
*   **Prompt:** *"Delete all current stubs and enable proxying to `https://dev-env-4.internal.com`."*
*   **Outcome:** Wixy is instantly reconfigured to point to a new shared environment.

## Advanced AI-Driven Workflows

Combining Wixy's MCP tools with an AI's reasoning capabilities allows for "Self-Healing Tests":
1.  **Detect:** A test fails because of a 404 from a dependency.
2.  **Analyze:** The AI uses `listStubs` to see if a mock exists.
3.  **Fix:** If missing, the AI uses `createStub` to provide a default successful response.
4.  **Verify:** The test is re-run and passes.
