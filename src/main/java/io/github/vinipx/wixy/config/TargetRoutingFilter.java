package io.github.vinipx.wixy.config;

import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.ManagedServer;
import io.github.vinipx.wixy.engine.ServerRegistryService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter that looks for X-Wixy-Target-Server header to temporarily override
 * the active WireMock engine for the duration of the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // After logging, before security
public class TargetRoutingFilter extends OncePerRequestFilter {

    private static final String TARGET_HEADER = "X-Wixy-Target-Server";
    
    private final ServerRegistryService registryService;
    private final EngineManager engineManager;

    public TargetRoutingFilter(ServerRegistryService registryService, EngineManager engineManager) {
        this.registryService = registryService;
        this.engineManager = engineManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String targetId = request.getHeader(TARGET_HEADER);

        if (targetId != null && !targetId.isBlank()) {
            try {
                if (targetId.equalsIgnoreCase("local")) {
                    // No override needed, or we could explicitly set local engine
                    // But usually local is the base active engine anyway
                } else {
                    UUID uuid = UUID.fromString(targetId);
                    Optional<ManagedServer> server = registryService.getById(uuid);
                    
                    if (server.isPresent()) {
                        engineManager.setRequestOverride(engineManager.getEngineForUrl(server.get().getUrl()));
                    }
                }
            } catch (Exception e) {
                // Ignore invalid header values, fallback to active engine
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            engineManager.clearRequestOverride();
        }
    }
}
