package io.github.vinipx.wixy.unit.config;

import io.github.vinipx.wixy.config.WebConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("WebConfig")
class WebConfigTest {

    private final WebConfig webConfig = new WebConfig();

    @Test
    @DisplayName("Should register resource handlers for static assets and documentation")
    void registerHandlers() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration mainRegistration = mock(ResourceHandlerRegistration.class);
        ResourceHandlerRegistration docsRegistration = mock(ResourceHandlerRegistration.class);
        ResourceChainRegistration chainRegistration = mock(ResourceChainRegistration.class);

        when(registry.addResourceHandler("/**")).thenReturn(mainRegistration);
        when(registry.addResourceHandler("/docs/**")).thenReturn(docsRegistration);
        
        when(mainRegistration.addResourceLocations(anyString())).thenReturn(mainRegistration);
        when(mainRegistration.resourceChain(anyBoolean())).thenReturn(chainRegistration);
        
        when(docsRegistration.addResourceLocations(anyString())).thenReturn(docsRegistration);
        when(docsRegistration.resourceChain(anyBoolean())).thenReturn(chainRegistration);

        webConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/docs/**");
        verify(registry).addResourceHandler("/**");
    }
}
