# Log Streaming Console Implementation Plan

To build a modern, high-performance terminal console for streaming WireMock engine logs in the Wixy Hub, you need a solution that handles high-throughput text rendering efficiently without locking up the browser, while also providing a sleek TUI (Terminal User Interface) aesthetic.

This document details the feature implementation plan, presenting two primary architectural options for the React (Vite) + Spring Boot stack.

## Architectural Options

### Option 1: WebSockets + `xterm.js` (The "Authentic Terminal" Approach)

This approach uses **`xterm.js`**, the industry-standard terminal emulator for the web (used by VS Code). Logs are streamed bidirectionally via WebSockets.

*   **Backend (Spring Boot):** Implement a `WebSocketConfigurer`. When an engine is started or connected to the Hub, attach a custom log appender or WireMock `RequestListener` that pipes formatted log strings to active WebSocket sessions based on the `engineId`.
*   **Frontend (React):** Wrap `xterm.js` in a React component. Use `xterm-addon-search` for dynamic string filtering and `xterm-addon-fit` to make it responsive.

**PROS:**
*   **Performance:** `xterm.js` uses Canvas/WebGL for rendering. It can handle massive streams of log data without choking the React DOM or the browser's main thread.
*   **Authentic TUI Feel:** It looks, feels, and acts exactly like a real terminal. Native support for ANSI escape codes (for colored log levels), text selection, and standard clipboard shortcuts (Cmd+C / Ctrl+C).
*   **Bidirectional Control:** You can send a "PAUSE" message over the WebSocket to stop the server from sending logs, saving network bandwidth.

**CONS:**
*   **Styling Rigidity:** Because it renders to a Canvas, you cannot easily style individual log lines with standard CSS or inject React components (like hover-over tooltips) into the log stream.
*   **Infrastructure:** WebSockets require stateful connections and specific configuration if you ever put Wixy behind a reverse proxy (like Nginx) or a load balancer.

### Option 2: Server-Sent Events (SSE) + React Virtualized List (The "React-Native" Approach)

This approach uses **Server-Sent Events (SSE)** for unidirectional log streaming and a virtualized list library (like `react-virtuoso` or `react-window`) to render the UI.

*   **Backend (Spring Boot):** Expose an endpoint returning an `SseEmitter` (e.g., `GET /api/engines/{id}/logs/stream`). The server pushes log objects (JSON) as events.
*   **Frontend (React):** Use the native browser `EventSource` API to consume the stream. Accumulate logs in a React state array. Render them using `react-virtuoso` with custom CSS (JetBrains Mono font, dark background) to mimic a TUI.

**PROS:**
*   **Simplicity & HTTP Native:** SSE operates over standard HTTP/1.1 or HTTP/2. No complex protocol upgrades or proxy issues.
*   **Deep React Integration:** Because logs are just React nodes, you can easily implement custom UI features: syntax highlighting for JSON payloads within the logs, clickable URLs, or collapsible JSON trees.
*   **Easy Filtering:** Filtering is as simple as applying a JS `.filter()` on your log array before passing it to the virtualized list.

**CONS:**
*   **Memory Management:** Even with virtualization, accumulating tens of thousands of log lines in a JS array can eventually cause memory bloat. You will need to implement a rolling buffer (e.g., keep only the last 5,000 lines).
*   **One-Way Traffic:** To "PAUSE" the stream, the client either has to completely disconnect the `EventSource` (which might miss logs you wanted to buffer) or just drop messages on the client side (wasting bandwidth).

## Recommendation

**Option 1 (WebSockets + xterm.js)** is recommended for a logging console. Log streams from mock engines can get incredibly noisy, especially when dumping large HTTP request/response payloads. `xterm.js` is built explicitly for this kind of raw text throughput, and it inherently provides the "tech/hacker" aesthetic required.

---

## Feature Implementation Plan (Based on xterm.js)

### Phase 1: Backend Log Capture & Streaming
1. **Log Interception:** Implement a WireMock `RequestListener` or a custom log Appender in `EngineManager.java` that intercepts incoming requests, responses, and engine lifecycle events.
2. **WebSocket Configuration:** Add Spring WebSocket dependencies. Create a `LogWebSocketHandler` that maps to `/ws/logs/{engineId}`.
3. **Broadcasting:** When a log event occurs for a specific engine, format it (adding ANSI color codes for HTTP methods or status codes) and broadcast it to all WebSocket sessions subscribed to that `engineId`.

### Phase 2: Frontend Terminal Component
1. **Dependencies:** Install `xterm`, `@xterm/addon-fit`, and `@xterm/addon-search` in the `frontend` project.
2. **Component Wrapper:** Create a `TerminalConsole.tsx` component. Initialize the `Terminal` instance in a `useEffect` hook, attaching it to a `div` ref.
3. **Theming:** Apply a modern TUI theme:
   ```javascript
   theme: {
     background: '#0d1117', // GitHub Dark or similar
     foreground: '#e6edf3',
     cursor: '#2f81f7',
     selectionBackground: '#388bfd33'
   },
   fontFamily: "'Fira Code', 'JetBrains Mono', monospace"
   ```

### Phase 3: Core Features Implementation
1. **Connecting the Stream:** Open a WebSocket connection to the backend. On `ws.onmessage`, call `terminal.write(event.data)`.
2. **Play/Pause Toggle:**
   * Create a state `isPaused`.
   * When paused, either send a pause frame to the server via WebSocket, or buffer incoming messages in a hidden JS array and stop calling `terminal.write()`.
   * On "Play", flush the buffer to the terminal and resume standard writing.
3. **Dynamic Filtering:**
   * Add a text input field above the terminal.
   * Bind the input to the `SearchAddon`. Use `searchAddon.findNext(query)` to highlight text.
   * *Alternatively*, if you want to completely hide non-matching lines (like a `grep`), you must manage a raw log buffer in React state, clear the terminal, and rewrite only matching lines to `xterm.js`.
4. **Copying:** `xterm.js` automatically supports native text selection and Cmd/Ctrl + C. You don't need to write custom logic for this.
5. **Download Logs:**
   * Maintain a raw text buffer of the logs in React state (or fetch it from `terminal.selectAll()` and `terminal.getSelection()`).
   * Create a "Download" button that triggers a blob creation:
     ```typescript
     const blob = new Blob([logBuffer], { type: 'text/plain' });
     const url = URL.createObjectURL(blob);
     // create temporary <a> tag, set href to url, set download to 'engine-logs.txt', and click()
     ```
