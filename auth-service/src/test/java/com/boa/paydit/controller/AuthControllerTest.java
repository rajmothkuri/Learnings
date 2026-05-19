package com.boa.paydit.controller;

import com.boa.paydit.dto.request.LoginRequestDto;
import com.boa.paydit.dto.request.RegisterRequestDto;
import com.boa.paydit.dto.response.AuthResponseDto;
import com.boa.paydit.exception.GlobalExceptionHandler;
import com.boa.paydit.service.AuthService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
            new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        var request = new LoginRequestDto("alice", "secret");
        var response = new AuthResponseDto("jwt-token", "USER");

        when(authService.login(any(LoginRequestDto.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerReturnsJwtToken() throws Exception {
        var request = new RegisterRequestDto("bob", "password", "USER");
        var response = new AuthResponseDto("register-token", "USER");

        when(authService.register(any(RegisterRequestDto.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("register-token"))
            .andExpect(jsonPath("$.role").value("USER"));
    }
}