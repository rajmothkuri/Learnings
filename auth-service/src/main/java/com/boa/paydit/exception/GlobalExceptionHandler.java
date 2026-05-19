package com.boa.paydit.exception;

import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.JwtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            GlobalExceptionHandler.class);

    @ExceptionHandler(
        InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(
        InvalidCredentialsException ex) {

        LOGGER.error(
            "Authentication failed: {}",
            ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex.getMessage());
    }

    @ExceptionHandler(
        BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(
        BadCredentialsException ex) {

        LOGGER.error(
            "Bad credentials: {}",
            ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Invalid username or password");
    }

    @ExceptionHandler(
        UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameExists(
        UsernameAlreadyExistsException ex) {

        LOGGER.warn(
            "Duplicate registration attempt: {}",
            ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ex.getMessage());
    }

    @ExceptionHandler(
        JwtException.class)
    public ResponseEntity<?> handleJwtException(
        JwtException ex) {

        LOGGER.error(
            "JWT processing failed: {}",
            ex.getMessage());

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Invalid or expired JWT token");
    }

    @ExceptionHandler(
        MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
        MethodArgumentNotValidException ex) {

        LOGGER.error(
            "Validation failed");

        Map<String, String> errors =
            new HashMap<>();

        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                errors.put(
                    error.getField(),
                    error.getDefaultMessage()));

        return ResponseEntity
            .badRequest()
            .body(errors);
    }
}