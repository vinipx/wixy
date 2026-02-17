package io.github.vinipx.wixy.unit.config;

import io.github.vinipx.wixy.config.TargetRoutingFilter;
import io.github.vinipx.wixy.engine.EngineManager;
import io.github.vinipx.wixy.engine.ManagedServer;
import io.github.vinipx.wixy.engine.ServerRegistryService;
import io.github.vinipx.wixy.engine.WireMockEngine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("TargetRoutingFilter")
class TargetRoutingFilterTest {

    private final ServerRegistryService registryService = mock(ServerRegistryService.class);
    private final EngineManager engineManager = mock(EngineManager.class);
    private final TargetRoutingFilter filter = new TargetRoutingFilter(registryService, engineManager);

    @Test
    @DisplayName("Should set override when valid header is present")
    void withValidHeader() throws Exception {
        UUID id = UUID.randomUUID();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(request.getHeader("X-Wixy-Target-Server")).thenReturn(id.toString());
        ManagedServer server = new ManagedServer(id, "Remote", "http://remote", ManagedServer.ServerType.REMOTE);
        when(registryService.getById(id)).thenReturn(Optional.of(server));
        
        WireMockEngine remoteEngine = mock(WireMockEngine.class);
        when(engineManager.getEngineForUrl(server.getUrl())).thenReturn(remoteEngine);

        filter.doFilter(request, response, chain);

        verify(engineManager).setRequestOverride(remoteEngine);
        verify(chain).doFilter(request, response);
        verify(engineManager).clearRequestOverride();
    }

    @Test
    @DisplayName("Should do nothing when header is missing")
    void withoutHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(request.getHeader("X-Wixy-Target-Server")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(engineManager, never()).setRequestOverride(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should do nothing when header is 'local'")
    void withLocalHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        
        when(request.getHeader("X-Wixy-Target-Server")).thenReturn("local");

        filter.doFilter(request, response, chain);

        verify(engineManager, never()).setRequestOverride(any());
        verify(chain).doFilter(request, response);
    }
}
