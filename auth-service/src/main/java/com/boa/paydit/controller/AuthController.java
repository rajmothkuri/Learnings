package com.boa.paydit.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.boa.paydit.dto.request.LoginRequestDto;
import com.boa.paydit.dto.request.RegisterRequestDto;
import com.boa.paydit.dto.response.AuthResponseDto;
import com.boa.paydit.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            AuthController.class);

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
        @Valid
        @RequestBody
        LoginRequestDto request) {

        LOGGER.info(
            "Login request received for user {}",
            request.username());

        AuthResponseDto response =
            service.login(request);

        LOGGER.info(
            "JWT token generated successfully for user {}",
            request.username());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
        @Valid
        @RequestBody
        RegisterRequestDto request) {

        LOGGER.info(
            "Registration request received for user {}",
            request.username());

        AuthResponseDto response =
            service.register(request);

        LOGGER.info(
            "User {} registered successfully",
            request.username());

        return ResponseEntity.ok(response);
    }
}