package com.boa.paydit.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boa.paydit.dto.request.LoginRequestDto;
import com.boa.paydit.dto.request.RegisterRequestDto;
import com.boa.paydit.dto.response.AuthResponseDto;
import com.boa.paydit.entity.UserEntity;
import com.boa.paydit.exception.InvalidCredentialsException;
import com.boa.paydit.exception.UsernameAlreadyExistsException;
import com.boa.paydit.repository.UserRepository;
import com.boa.paydit.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
implements AuthService {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            AuthServiceImpl.class);

    private final UserRepository repository;

    private final JwtTokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(
        LoginRequestDto request) {

        LOGGER.info(
            "Authenticating user {}",
            request.username());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()));

        UserEntity user =
            repository.findByUsername(
                request.username())
            .orElseThrow(() -> {

                LOGGER.error(
                    "Invalid credentials for user {}",
                    request.username());

                return new InvalidCredentialsException(
                    "Invalid username/password");
            });

        String token =
            tokenProvider.generateToken(
                user.getUsername(),
                user.getRole());

        LOGGER.info(
            "JWT token generated for user {}",
            request.username());

        return new AuthResponseDto(
            token,
            user.getRole());
    }

    @Override
    @Transactional
    public AuthResponseDto register(
        RegisterRequestDto request) {

        LOGGER.info(
            "Registering user {}",
            request.username());

        repository.findByUsername(request.username())
            .ifPresent(existing -> {
                LOGGER.warn(
                    "Registration failed because username {} already exists",
                    request.username());
                throw new UsernameAlreadyExistsException(
                    "Username already exists");
            });

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setPassword(
            passwordEncoder.encode(request.password()));
        newUser.setRole(request.role());

        UserEntity savedUser =
            repository.save(newUser);

        String token =
            tokenProvider.generateToken(
                savedUser.getUsername(),
                savedUser.getRole());

        LOGGER.info(
            "New user {} registered successfully",
            savedUser.getUsername());

        return new AuthResponseDto(
            token,
            savedUser.getRole());
    }
}