package com.boa.paydit.exception;

public sealed interface AuthException
    permits InvalidCredentialsException,
            UsernameAlreadyExistsException {
}
