package com.boa.paydit.dto.response;

public record AuthResponseDto(
    String token,
    String role) {
}