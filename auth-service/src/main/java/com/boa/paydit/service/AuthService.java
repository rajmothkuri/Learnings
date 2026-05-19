package com.boa.paydit.service;

import com.boa.paydit.dto.request.LoginRequestDto;
import com.boa.paydit.dto.request.RegisterRequestDto;
import com.boa.paydit.dto.response.AuthResponseDto;

public interface AuthService {

    AuthResponseDto login(
        LoginRequestDto request);

    AuthResponseDto register(
        RegisterRequestDto request);
}