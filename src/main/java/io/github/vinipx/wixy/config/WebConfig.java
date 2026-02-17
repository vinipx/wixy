package io.github.vinipx.wixy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Configuration for serving the React frontend, Docusaurus documentation,
 * and handling SPA routing.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple forward for /docs/
        registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
        // Redirect /docs to /docs/
        registry.addRedirectViewController("/docs", "/docs/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Specific handler for Documentation
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/")
                .resourceChain(true);

        // 2. Main Dashboard & Static Assets Handler
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // Ignore API calls and documentation (handled above)
                        if (resourcePath.startsWith("wixy/admin") || 
                            resourcePath.startsWith("actuator") || 
                            resourcePath.startsWith("docs")) {
                            return null;
                        }

                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // Forward to root index.html for React Router
                        return location.createRelative("index.html");
                    }
                });
    }
}
