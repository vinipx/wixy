---
sidebar_position: 1
title: Dashboard & Control
---

# Engine Control Dashboard

The Dashboard is your command center for orchestrating individual WireMock engines. It provides a unified interface to control traffic flow, record requests, and manage stub mappings.

## Engine Selector

At the top of the Dashboard, you'll find the **Managed Engine** dropdown. 
- This allows you to instantly switch the Hub's focus between different engines in your registry.
- Switching engines here updates all data on the page (Proxy, Recorder, Stubs) to reflect the newly selected environment.
- The Hub maintains connection state for each engine, showing you the active port and connectivity status in real-time.

## Core Controls

### 1. Proxy Mode
Toggle between "Stub-only" and "Smart Proxy" modes.
- **Enabled:** Any request not matching an existing stub will be forwarded to the **Target Upstream URL**.
- **Disabled:** Unmatched requests return a 404 (or the default WireMock "No stub found" response).

### 2. Traffic Recorder
The Hub can act as a bridge to capture real-world traffic.
- **Start Recording:** Begins capturing all proxied traffic.
- **Stop Recording:** Finalizes the capture session and automatically creates persistent stub mappings for every unique request/response pair encountered.

### 3. Live Stats
A quick-view panel showing:
- **Active Stubs:** Total number of mappings currently registered on the engine.
- **Proxy Status:** Current operational mode (ON/OFF).
- **System Health:** Verification that the Hub can communicate with the engine's admin plane.

## Stub Management

The lower section of the Dashboard contains the **Active Stub Mappings** table.
- **Method & URL:** Displays the HTTP verb and path pattern for every stub.
- **Priority:** Shows the matching weight (default is 5).
- **Actions:**
    - **Edit:** Open the JSON editor to modify stub behavior (headers, body, status).
    - **View:** Read-only inspection of the mapping.
    - **Delete:** Permanently remove the stub from the engine.

The Dashboard uses **Smart Caching** and background polling to ensure that management operations are snappy, even when orchestrating remote nodes over high-latency networks.
