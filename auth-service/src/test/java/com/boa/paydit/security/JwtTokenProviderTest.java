package com.boa.paydit.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
            "ChangeThisSecretForDevUseBetter1234567890!",
            3600000L);
    }

    @Test
    void shouldGenerateAndValidateJwtToken() {
        String token = tokenProvider.generateToken("alice", "USER");

        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("alice");

        Authentication authentication =
            tokenProvider.getAuthentication(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
            .extracting("authority")
            .contains("ROLE_USER");
    }
}