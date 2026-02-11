package io.github.vinipx.wixy.mcp;

import io.github.vinipx.wixy.service.StubService;
import io.github.vinipx.wixy.service.ProxyService;
import io.github.vinipx.wixy.service.RecordingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service providing MCP tools for interacting with Wixy.
 */
@Service
public class WixyMcpService {

    private final StubService stubService;
    private final ProxyService proxyService;
    private final RecordingService recordingService;

    public WixyMcpService(StubService stubService, ProxyService proxyService, RecordingService recordingService) {
        this.stubService = stubService;
        this.proxyService = proxyService;
        this.recordingService = recordingService;
    }

    @Tool(description = "List all active WireMock stub mappings in Wixy")
    public String listStubs() {
        return stubService.listAll().toString();
    }

    @Tool(description = "Create a new stub mapping from a WireMock JSON string")
    public String createStub(@ToolParam(description = "The WireMock stub definition in JSON format") String json) {
        return stubService.create(json).toString();
    }

    @Tool(description = "Delete a specific stub mapping by its UUID")
    public void deleteStub(@ToolParam(description = "The UUID of the stub") String id) {
        stubService.delete(UUID.fromString(id));
    }

    @Tool(description = "Enable proxying traffic to a target URL for unmatched requests")
    public void enableProxy(@ToolParam(description = "The upstream target URL (e.g. https://api.prod.com)") String targetUrl) {
        proxyService.enableProxy(targetUrl);
    }

    @Tool(description = "Disable proxy mode")
    public void disableProxy() {
        proxyService.disableProxy();
    }

    @Tool(description = "Start recording traffic and automatically creating stubs")
    public void startRecording(@ToolParam(description = "Optional target URL to record from") String targetUrl) {
        recordingService.startRecording(targetUrl);
    }

    @Tool(description = "Stop the current recording session and return the number of captured stubs")
    public String stopRecording() {
        return "Captured " + recordingService.stopRecording().getStubMappings().size() + " stubs.";
    }
}
