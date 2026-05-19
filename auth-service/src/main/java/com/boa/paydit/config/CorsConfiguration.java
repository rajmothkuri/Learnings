package com.boa.paydit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfiguration {

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource(){

        org.springframework.web.cors.CorsConfiguration
        configuration =
        new org.springframework.web.cors.CorsConfiguration();

        configuration.addAllowedOrigin("*");

        configuration.addAllowedHeader("*");

        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration);

        return source;
    }
}