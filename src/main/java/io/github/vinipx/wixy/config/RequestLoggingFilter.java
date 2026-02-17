package io.github.vinipx.wixy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for logging incoming HTTP requests to aid in debugging.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/wixy/admin")) {
            log.info("Incoming Request: {} {} (Content-Type: {})", method, path, request.getContentType());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (path.startsWith("/wixy/admin")) {
                log.info("Outgoing Response: {} {} -> Status {}", method, path, response.getStatus());
            }
        }
    }
}
