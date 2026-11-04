package com.rapidm3.employeedirectory.security;

import com.rapidm3.employeedirectory.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Rejects every API request without a valid Firebase ID token (401).
 * React route guards only hide UI; this filter is what actually protects the data.
 */
public class FirebaseAuthFilter extends OncePerRequestFilter {

    public static final String UID_ATTRIBUTE = "firebaseUid";
    private static final String BEARER = "Bearer ";

    private final TokenVerifier verifier;
    private final ObjectMapper objectMapper;

    public FirebaseAuthFilter(TokenVerifier verifier, ObjectMapper objectMapper) {
        this.verifier = verifier;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight carries no Authorization header; the CORS filter answers it.
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            unauthorized(response, "Sign in required.");
            return;
        }
        try {
            request.setAttribute(UID_ATTRIBUTE, verifier.verify(header.substring(BEARER.length())));
        } catch (InvalidTokenException e) {
            unauthorized(response, "Your session is invalid or has expired. Please sign in again.");
            return;
        }
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(401, "UNAUTHORIZED", message));
    }
}
