package io.github.vinipx.wixy.integration.mcp;

import io.github.vinipx.wixy.integration.BaseIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Integration tests for the Model Context Protocol (MCP) server integration.
 * This validates that WIXY correctly exposes its management plane to AI agents.
 */
@Tag("integration")
@DisplayName("MCP Server Integration")
class McpServerIT extends BaseIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    @DisplayName("MCP server bean should be initialized in context")
    void mcpServerBeanExists() {
        assertThat(context.containsBean("mcpSyncServer")).isTrue();
    }

    @Test
    @DisplayName("All Wixy tools should be registered for AI discovery")
    void toolsRegistered() {
        assertThat(toolCallbackProvider).isNotNull();
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        
        List<String> names = Arrays.stream(callbacks)
                .map(cb -> cb.getToolDefinition().name())
                .toList();

        assertThat(names).contains(
                "listStubs", 
                "createStub", 
                "deleteStub", 
                "enableProxy", 
                "disableProxy", 
                "startRecording", 
                "stopRecording"
        );
    }

    @Test
    @DisplayName("GET /sse should be active and return session stream")
    void sseEndpointResponsive() {
        // Set a short timeout because SSE streams stay open indefinitely
        RestAssured.config = RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", 2000)
                .setParam("http.socket.timeout", 2000));

        try {
            given()
                    .when()
                    .get("/sse")
                    .then()
                    .statusCode(200)
                    .contentType(containsString("text/event-stream"));
        } catch (Exception e) {
            // SocketTimeoutException is expected for persistent streams.
            boolean isTimeout = e.getMessage() != null && e.getMessage().contains("Read timed out");
            boolean isSocketTimeout = e.getCause() instanceof java.net.SocketTimeoutException;
            
            if (!isTimeout && !isSocketTimeout) {
                throw e;
            }
        }
    }
}
