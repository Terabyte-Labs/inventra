package mx.terabyte.labs.inventra.config.web;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Populates MDC with request-scoped values to make logs traceable in production.
 * - requestId: taken from X-Request-Id header if present, otherwise generated
 * - clientIp: X-Forwarded-For or remoteAddr
 * - username: authenticated principal name when available
 * Also returns X-Request-Id header in the response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String CLIENT_IP = "clientIp";
    private static final String USERNAME = "username";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(HEADER_REQUEST_ID))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString());

        String clientIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .filter(h -> !h.isBlank())
                .map(h -> h.split(",")[0].trim())
                .orElseGet(request::getRemoteAddr);

        try {
            MDC.put(REQUEST_ID, requestId);
            MDC.put(CLIENT_IP, clientIp);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                MDC.put(USERNAME, auth.getName());
            }

            // Echo request id to client for cross-service tracing
            response.setHeader(HEADER_REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}


