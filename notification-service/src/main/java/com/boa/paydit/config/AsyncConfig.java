package com.boa.paydit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async configuration for CompletableFuture and async processing
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Async configuration for @Async annotations
}
