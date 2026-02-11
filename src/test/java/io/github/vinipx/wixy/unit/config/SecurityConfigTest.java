package io.github.vinipx.wixy.unit.config;

import io.github.vinipx.wixy.config.SecurityConfig;
import io.github.vinipx.wixy.config.WixyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SecurityConfig} and the inner {@code ApiKeyFilter}.
 */
@Tag("unit")
@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private static final String API_KEY_HEADER = "X-Wixy-Api-Key";
    private static final String VALID_KEY = "test-secret-key";

    private SecurityConfig.ApiKeyFilter createFilter(String expectedKey) {
        return new SecurityConfig.ApiKeyFilter(expectedKey);
    }

    private HttpServletRequest mockRequest(String uri, String apiKey) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getHeader(API_KEY_HEADER)).thenReturn(apiKey);
        return req;
    }

    private HttpServletResponse mockResponse() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return resp;
    }

    // ── Allowed paths (no auth required) ────────────────────────

    @Nested
    @DisplayName("Allow-listed paths")
    class AllowedPaths {

        @Test
        @DisplayName("should allow /actuator/health without API key")
        void actuatorHealth() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/actuator/health", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
            verify(resp, never()).setStatus(anyInt());
        }

        @Test
        @DisplayName("should allow /actuator/info without API key")
        void actuatorInfo() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/actuator/info", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }

        @Test
        @DisplayName("should allow /swagger-ui.html without API key")
        void swaggerUi() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/swagger-ui.html", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }

        @Test
        @DisplayName("should allow /v3/api-docs without API key")
        void apiDocs() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/v3/api-docs", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }

        @Test
        @DisplayName("should allow sub-paths under allowed prefixes")
        void subPaths() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/actuator/health/liveness", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }
    }

    // ── Valid API key ───────────────────────────────────────────

    @Nested
    @DisplayName("Valid API key")
    class ValidKey {

        @Test
        @DisplayName("should allow request with correct API key")
        void correctKey() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/wixy/admin/mappings", VALID_KEY);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
            verify(resp, never()).setStatus(anyInt());
        }
    }

    // ── Invalid / missing API key ───────────────────────────────

    @Nested
    @DisplayName("Invalid or missing API key")
    class InvalidKey {

        @Test
        @DisplayName("should reject request with no API key header")
        void missingKey() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/wixy/admin/mappings", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(resp).setStatus(401);
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("should reject request with wrong API key")
        void wrongKey() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/wixy/admin/mappings", "wrong-key");
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(resp).setStatus(401);
            verify(chain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("should reject request with empty API key")
        void emptyKey() throws Exception {
            var filter = createFilter(VALID_KEY);
            var req = mockRequest("/wixy/admin/mappings", "");
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(resp).setStatus(401);
            verify(chain, never()).doFilter(any(), any());
        }
    }

    // ── No key configured (pass-through) ────────────────────────

    @Nested
    @DisplayName("No key configured (misconfiguration pass-through)")
    class NoKeyConfigured {

        @Test
        @DisplayName("should pass through when expected key is null")
        void nullExpectedKey() throws Exception {
            var filter = createFilter(null);
            var req = mockRequest("/wixy/admin/mappings", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }

        @Test
        @DisplayName("should pass through when expected key is blank")
        void blankExpectedKey() throws Exception {
            var filter = createFilter("   ");
            var req = mockRequest("/wixy/admin/mappings", null);
            var resp = mockResponse();
            var chain = mock(FilterChain.class);

            filter.doFilter(req, resp, chain);

            verify(chain).doFilter(req, resp);
        }
    }

    // ── Accessor ────────────────────────────────────────────────

    @Nested
    @DisplayName("Accessor methods")
    class Accessors {

        @Test
        @DisplayName("getExpectedApiKey should return the configured key")
        void getExpectedApiKey() {
            var filter = createFilter("my-key");
            assertThat(filter.getExpectedApiKey()).isEqualTo("my-key");
        }
    }

    // ── Bean creation ───────────────────────────────────────────

    @Nested
    @DisplayName("SecurityConfig bean factory")
    class BeanFactory {

        @Test
        @DisplayName("should create ApiKeyFilter with correct expected key")
        void createApiKeyFilter() {
            var config = new SecurityConfig();
            var props = new WixyProperties();
            props.getSecurity().setEnabled(true);
            props.getSecurity().setApiKey("from-config");

            SecurityConfig.ApiKeyFilter filter = config.apiKeyFilter(props);

            assertThat(filter).isNotNull();
            assertThat(filter.getExpectedApiKey()).isEqualTo("from-config");
        }
    }
}
