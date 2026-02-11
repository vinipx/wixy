package io.github.vinipx.wixy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Optional API-key security filter.
 * Activated only when {@code wixy.security.enabled=true}.
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String API_KEY_HEADER = "X-Wixy-Api-Key";

    /**
     * Paths that are always allowed without authentication.
     */
    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui.html",
            "/v3/api-docs"
    );

    @Bean
    @ConditionalOnProperty(name = "wixy.security.enabled", havingValue = "true")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApiKeyFilter apiKeyFilter(WixyProperties properties) {
        log.info("🔐 API-key security filter enabled");
        return new ApiKeyFilter(properties.getSecurity().getApiKey());
    }

    /**
     * Filter that validates the {@code X-Wixy-Api-Key} header on every request
     * except for explicitly allowed paths.
     */
    public static class ApiKeyFilter extends OncePerRequestFilter {

        private final String expectedApiKey;

        public ApiKeyFilter(String expectedApiKey) {
            this.expectedApiKey = expectedApiKey;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String path = request.getRequestURI();

            // Allow-listed paths pass through
            if (isAllowed(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String providedKey = request.getHeader(API_KEY_HEADER);

            if (expectedApiKey == null || expectedApiKey.isBlank()) {
                // No key configured — pass through (misconfiguration, but don't block)
                filterChain.doFilter(request, response);
                return;
            }

            if (!expectedApiKey.equals(providedKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid X-Wixy-Api-Key header\"}"
                );
                return;
            }

            filterChain.doFilter(request, response);
        }

        private boolean isAllowed(String path) {
            return ALLOWED_PATHS.stream().anyMatch(path::startsWith);
        }

        public String getExpectedApiKey() {
            return expectedApiKey;
        }
    }
}
