package com.boa.paydit.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventLogger {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(AuthenticationEventLogger.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        LOGGER.info(
            "Authentication success: principal={}, details={}, authenticated={}",
            authentication.getName(),
            authentication.getDetails(),
            authentication.isAuthenticated());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        LOGGER.warn(
            "Authentication failure: principal={}, provider={}, error={}",
            authentication != null ? authentication.getName() : "unknown",
            event.getAuthentication().getDetails(),
            event.getException().getMessage());
    }
}
