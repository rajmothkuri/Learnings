package com.boa.paydit.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
extends OncePerRequestFilter {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
    throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            if (tokenProvider.validateToken(token)) {
                SecurityContextHolder.getContext()
                    .setAuthentication(
                        tokenProvider.getAuthentication(token));
                LOGGER.debug(
                    "JWT authentication established for uri {}",
                    request.getRequestURI());
            } else {
                LOGGER.warn(
                    "Invalid or expired JWT token received for uri {}",
                    request.getRequestURI());
            }
        } else {
            LOGGER.debug(
                "No JWT token provided for uri {}",
                request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean isAuthApi = path.matches("^/api/[^/]+/auth($|/.*)");
        boolean isOAuth2 = path.startsWith("/oauth2") || path.startsWith("/login");
        return isAuthApi || path.startsWith("/actuator") || isOAuth2;
    }
}