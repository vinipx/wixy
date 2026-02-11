---
sidebar_position: 3
title: Client Setup
---

# Connecting AI Clients

To use WIXY's AI features, you need to connect an MCP-compatible client to the WIXY management port.

## 1. Claude Desktop

To add WIXY to Claude Desktop, modify your configuration file:
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Add WIXY to the `mcpServers` object:

```json title="claude_desktop_config.json"
{
  "mcpServers": {
    "wixy": {
      "command": "curl",
      "args": ["-s", "http://localhost:8080/wixy/mcp"]
    }
  }
}
```

:::tip
Since WIXY uses SSE over HTTP, we use `curl` to pipe the event stream to Claude.
:::

## 2. Cursor / IDEs

For IDEs like **Cursor** or **Windsurf**:
1. Open **Settings** > **Features** > **MCP**.
2. Click **+ Add New MCP Server**.
3. Name: `Wixy`.
4. Type: `sse`.
5. URL: `http://localhost:8080/wixy/mcp`.

## 3. MCP Inspector (Debugging)

The MCP Inspector is the best way to verify that your tools are correctly exposed.

```bash
npx @modelcontextprotocol/inspector http://localhost:8080/wixy/mcp
```

This will launch a web interface where you can:
- List all available tools (`listStubs`, `createStub`, etc.)
- Run tools manually to see their outputs.
- Inspect the JSON-RPC traffic.

## 4. Custom Python/Node Clients

You can connect your own custom agents using the official SDKs:

```python title="Python Example"
from mcp import ClientSession
from mcp.client.sse import sse_client

async with sse_client("http://localhost:8080/wixy/mcp") as (read, write):
    async with ClientSession(read, write) as session:
        await session.initialize()
        tools = await session.list_tools()
        print(f"Discovered {len(tools.tools)} tools")
```
