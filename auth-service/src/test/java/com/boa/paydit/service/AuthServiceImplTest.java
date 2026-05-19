package com.boa.paydit.service;

import java.util.Collections;
import java.util.Optional;

import com.boa.paydit.dto.request.LoginRequestDto;
import com.boa.paydit.dto.request.RegisterRequestDto;
import com.boa.paydit.dto.response.AuthResponseDto;
import com.boa.paydit.entity.UserEntity;
import com.boa.paydit.repository.UserRepository;
import com.boa.paydit.security.JwtTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Mock
    private UserRepository repository;

    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenProvider = new JwtTokenProvider(
            "ChangeThisSecretForDevUseBetter1234567890!",
            3600000L);
        authService = new AuthServiceImpl(
            repository,
            tokenProvider,
            authenticationManager,
            passwordEncoder);
    }

    @Test
    void shouldRegisterNewUserAndReturnToken() {
        when(repository.findByUsername("alice"))
            .thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret"))
            .thenReturn("encoded-secret");
        when(repository.save(any(UserEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.register(
            new RegisterRequestDto("alice", "secret", "USER"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo("USER");
        verify(repository).save(any(UserEntity.class));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        var user = new UserEntity();
        user.setUsername("alice");
        user.setPassword("encoded-secret");
        user.setRole("USER");

        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken(
                "alice", null, Collections.emptyList()));
        when(repository.findByUsername("alice"))
            .thenReturn(Optional.of(user));

        AuthResponseDto response = authService.login(
            new LoginRequestDto("alice", "secret"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo("USER");
        verify(authenticationManager).authenticate(any());
    }
}