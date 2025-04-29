package org.cursework.component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    @Value("${path.storage}")
    private String fullPath;

    private String validApiKeys;

    @PostConstruct
    public void init() {
        this.validApiKeys = FileDirectory.readFileKey(fullPath);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !validApiKeys.contains(apiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid or missing API Key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

