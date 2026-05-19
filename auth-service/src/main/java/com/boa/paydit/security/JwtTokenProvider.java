package com.boa.paydit.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            JwtTokenProvider.class);

    private final Key key;

    private final long expirationMs;

    public JwtTokenProvider(
        @Value("${jwt.secret}")
        String secret,
        @Value("${jwt.expiration-ms}")
        long expirationMs) {

        this.key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(
        String username,
        String role) {

        LOGGER.info(
            "Generating JWT token for user {} with role {}",
            username,
            role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", List.of(role))
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(
        String token) {

        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.error(
                "JWT validation failed: {}",
                ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(
        String token) {

        return parseClaims(token).getSubject();
    }

    public Authentication getAuthentication(
        String token) {

        var claims = parseClaims(token);
        var roleValues = getRoles(claims);

        var authorities = roleValues.stream()
            .map(role -> new SimpleGrantedAuthority(
                "ROLE_" + role))
            .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
            getUsernameFromToken(token),
            null,
            authorities);
    }

    private Claims parseClaims(
        String token) {

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    @SuppressWarnings("unchecked")
    private List<String> getRoles(
        Claims claims) {

        Object roles = claims.get("roles");

        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(it -> it.replaceFirst("^ROLE_", ""))
                .collect(Collectors.toList());
        }

        return List.of();
    }
}