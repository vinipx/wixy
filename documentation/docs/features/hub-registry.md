---
sidebar_position: 2
title: Hub Registry
---

# Fleet Registry

The Registry is the central database of all WireMock engines managed by the Hub. It allows you to organize, monitor, and quickly access every environment in your fleet.

## Overview

A "Server" in WIXY can be either:
- **Internal (Embedded):** The WireMock instance running inside the Hub process (default: port 9090).
- **Remote:** Any external WireMock instance accessible via HTTP (Docker containers, staging servers, standalone JARs).

## Server Cards

Each engine in the registry is represented by a card showing:
- **Engine Name:** User-defined label for the environment.
- **Admin URL:** The technical endpoint used by the Hub to manage stubs and proxying.
- **Connectivity Status:** A real-time indicator (Online/Unreachable) showing if the Hub can talk to the engine.
- **Type Icon:** Distinguishes between the local CPU icon (Internal) and the Globe icon (Remote).

## Actions

### 1. Config
Clicking **Config** makes that engine the "Active Engine" for the Hub and routes you directly to the **Dashboard**. From there, you can:
- Enable/Disable proxying for that specific engine.
- Manage its stub mappings.
- Start or stop traffic recording.

### 2. Logs
Clicking **Logs** makes that engine the "Active Engine" and routes you to the **Live Logs** tab, automatically subscribing you to its real-time traffic stream.

### 3. Management
- **Add Remote Server:** Use the "+" button to register a new external WireMock instance by providing its name and base URL.
- **Delete:** Remove a remote engine from your registry (internal local engines cannot be deleted).

## Persistence

The registry is persistent. By default, it is saved to your local user directory:
`${user.home}/.wixy/servers.json`

This ensures that your fleet configuration remains intact even after restarting the Hub or updating the application.
