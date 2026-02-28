---
sidebar_position: 3
title: Live Logs
---

# Live Logs Console

The WIXY Hub features a high-performance, real-time log streaming console that provides deep visibility into the HTTP traffic and internal events of your managed WireMock engines.

## Overview

The Log Console is built using **xterm.js**, providing an authentic Terminal User Interface (TUI) experience directly in your browser. It handles high-throughput traffic without impacting browser performance, thanks to its canvas-based rendering engine.

### Key Features

- **Real-time Streaming:** Logs are pushed from the Hub to the UI via WebSockets for sub-millisecond latency.
- **Background Persistence:** The Hub maintains log buffers in the background. You can switch tabs or navigate away, and your logs will still be there when you return.
- **Multi-Engine Support:** Seamlessly switch between viewing logs for the local embedded server or any remote engine in your fleet.
- **Visual ANSI Coloring:** HTTP methods, status codes, and timestamps are color-coded for rapid diagnostic scanning.
- **High Scrollback:** Maintains a buffer of up to 10,000 lines per engine.

## Usage Guide

### Selecting an Engine
At the top right of the Logs tab, you can see which engine is currently being watched. 
- The console defaults to the **Active Engine** selected in the Dashboard.
- You can use the **Reset** button to jump back to the local embedded server logs at any time.

### Toolbar Controls

- **Play/Pause:** Suspend the live stream to inspect a specific event. While paused, the Hub continues to buffer new logs in the background; they will be "flushed" to the terminal when you resume.
- **Clear:** Wipes the current terminal window and resets the background buffer for the current session.
- **Search:** Use the search bar to find and highlight specific strings (URLs, JSON fields, error messages) across the entire scrollback buffer.
- **Export:** Download the entire captured log history as a `.txt` file for offline analysis or bug reporting.

## Technical Architecture

The logging system operates differently depending on the engine type:

1.  **Local Engine:** The Hub attaches a `RequestListener` and a custom `Notifier` to the embedded WireMock instance. Every event is instantly broadcasted to the active WebSocket session.
2.  **Remote Engines:** For remote nodes, the Hub runs a **Background Log Poller**. When a user "subscribes" to a remote engine's logs, the Hub periodically fetches the remote's *Serve Events* journal and streams new entries to the UI.

### WebSocket Endpoint
The WebSocket endpoint is mapped to:
`ws://<hub-host>:8080/api/ws/logs/{engineId}`
