---
sidebar_position: 5
title: Troubleshooting
---

# MCP Troubleshooting

Common issues when using the Model Context Protocol with WIXY.

## Connection Failures

### 1. "Failed to connect to http://localhost:8080/wixy/mcp"
- **Check WIXY:** Is the application running? (`./gradlew bootRun`)
- **Check Port:** Are you using the correct management port (default 8080)?
- **Check Logs:** Look for "Starting MCP SSE Server" in the console output.

### 2. Claude Desktop showing "Error"
- **Path Issue:** Ensure the `curl` command is in your system PATH.
- **Config JSON:** Check for syntax errors in `claude_desktop_config.json`.
- **Restart:** You must **Restart** Claude Desktop completely after editing the config.

## Tool Issues

### 1. Tools are not visible in the AI chat
- **Handshake Failed:** Run the **MCP Inspector** to see if the server responds to the `listTools` request.
- **Profiles:** Ensure you aren't running with a profile that disables MCP.

### 2. `createStub` fails with "Invalid JSON"
- **Escape Characters:** Some AI models might forget to escape double quotes inside the JSON string parameter.
- **Validation:** WIXY validates the WireMock schema. Ask the AI to: *"Review the WireMock documentation and fix the JSON schema for this stub."*

## Security Errors

### 1. "401 Unauthorized"
- **API Key:** If security is enabled, you must provide the `X-Wixy-Api-Key` header.
- **Client Support:** If your client doesn't support custom headers for SSE, you may need to disable security temporarily for local AI testing:
  ```bash
  export WIXY_SECURITY_ENABLED=false
  ```

---

## Still having issues?

1. Use the **MCP Inspector** to isolate if the issue is in WIXY or the AI Client.
2. Check the [GitHub Issues](https://github.com/vinipx/wixy/issues) for similar reports.
3. Enable DEBUG logs for more detail:
   ```yaml
   logging:
     level:
       org.springframework.ai.mcp: DEBUG
   ```
