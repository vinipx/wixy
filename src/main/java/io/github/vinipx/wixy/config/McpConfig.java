package io.github.vinipx.wixy.config;

import io.github.vinipx.wixy.mcp.WixyMcpService;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider wixyMcpTools(WixyMcpService wixyMcpService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(wixyMcpService)
                .build();
    }
}
