---
sidebar_position: 4
title: Use Cases & Examples
---

# MCP Use Cases

This page provides practical examples of how to interact with WIXY using an AI agent.

## Available Tools Reference

Your AI agent automatically understands these tools through their JSON Schema:

- `listStubs`: Get all active mappings.
- `createStub(json)`: Add a new mapping.
- `deleteStub(id)`: Remove a mapping.
- `enableProxy(url)`: Turn on upstream forwarding.
- `disableProxy`: Turn off forwarding.
- `startRecording(url)`: Capture traffic.
- `stopRecording`: Save captured traffic as stubs.

---

## 👨‍💻 For Developers: Rapid Scaffolding

**Scenario:** You are building a checkout page and the payment service isn't ready.

*   **User:** *"Claude, I need a mock for the payment service. It's a POST to `/api/v1/pay`. It should accept a JSON with `amount` and `currency`. If successful, return a 201 Created with a transaction ID like `TXN-XXXX`."*
*   **AI Action:** Generates WireMock JSON and calls `createStub`.
*   **AI Result:** *"I've created the payment mock for you. You can now send POST requests to port 9090."*

---

## 🧪 For QA: Environment Management

**Scenario:** You need to capture a complex state from a staging environment.

*   **User:** *"Start recording traffic from `https://staging.acme.com`. I'm going to run the 'Forgot Password' flow now."*
*   **User Action:** (Performs the UI steps)
*   **User:** *"Stop recording and tell me what you captured."*
*   **AI Action:** Calls `stopRecording`, audits the new stubs.
*   **AI Result:** *"Recording stopped. I captured 4 stubs including `/api/auth/reset` and `/api/mail/sent`. They are now active in WIXY for offline use."*

---

## 🛡️ For DevOps: Resilience Testing

**Scenario:** You want to see how the system handles a partial outage.

*   **User:** *"I want to test our 'circuit breaker' implementation. Make the inventory service at `/api/inventory/.*` return a 503 error with a 2-second delay for every request."*
*   **AI Action:** Creates a high-priority stub with `fixedDelayMilliseconds` and a 503 status.
*   **AI Result:** *"The inventory service is now simulating a slow failure state. You can verify your circuit breakers now."*

---

## 🤖 Advanced: Self-Healing Tests

AI agents can use WIXY's tools to fix test environments autonomously:

1.  **AI:** *"I see the integration test for 'User Profile' failed with a 404."*
2.  **AI:** (Calls `listStubs`) *"The stubs are present, but the path is `/v1/users` while the code is calling `/v2/users`."*
3.  **AI:** (Calls `createStub`) *"I've added a mapping for `/v2/users` to match the new API version. Retrying tests..."*
