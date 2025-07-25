package org.cursework.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cursework.storage.FileDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyGuard extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyGuard.class);

    @Value("${path.storage}")
    private String storagePath;

    private String validApiKey;
    private boolean initialized = false;
    private final Object lock = new Object();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if (!initialized) {
            initializeApiKey();
        }

        if (validApiKey == null) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.getWriter().write("API Key system not ready");
            return;
        }

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("bucket_storage_api_key".equals(cookie.getName())) {
                    apiKey = cookie.getValue();
                    break;
                }
            }
        }

        if (apiKey == null || !apiKey.equals(validApiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid or missing API Key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/auth");
    }

    private void initializeApiKey() {
        synchronized (lock) {
            if (!initialized) {
                try {
                    this.validApiKey = FileDirectory.readFileKey(storagePath);
                    log.info("API Key initialized successfully");
                } finally {
                    initialized = true;
                }
            }
        }
    }
}
