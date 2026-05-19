package com.boa.paydit.security;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.boa.paydit.dto.response.AuthResponseDto;
import com.boa.paydit.entity.UserEntity;
import com.boa.paydit.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler
implements AuthenticationSuccessHandler {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            OAuth2LoginSuccessHandler.class);

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication)
    throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Unsupported authentication type");
            return;
        }

        OAuth2AuthenticationToken oauth2Token =
            (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();

        String username = resolveUsername(oauth2User, oauth2Token);
        UserEntity user = userRepository.findByUsername(username)
            .orElseGet(() -> createLocalUser(username));

        String token = tokenProvider.generateToken(
            user.getUsername(),
            user.getRole());

        LOGGER.info(
            "OAuth2 login successful for {} and JWT issued",
            username);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
            new AuthResponseDto(token, user.getRole())));
    }

    private String resolveUsername(
        OAuth2User oauth2User,
        OAuth2AuthenticationToken token) {

        Map<String, Object> attributes = oauth2User.getAttributes();

        return Optional.ofNullable((String) attributes.get("email"))
            .orElseGet(token::getName);
    }

    private UserEntity createLocalUser(String username) {
        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(
            passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole("USER");

        LOGGER.info(
            "Creating local user record for OAuth2 user {}",
            username);

        return userRepository.save(newUser);
    }
}
