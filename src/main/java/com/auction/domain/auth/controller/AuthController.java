package com.auction.domain.auth.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auth.dto.request.LoginRequestDto;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.auth.dto.response.LoginResponseDto;
import com.auction.domain.auth.dto.response.SignupResponseDto;
import com.auction.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/v1/signup")
    public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        return ApiResponse.ok(authService.createUser(signupRequest));
    }

    @PostMapping("/v1/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ApiResponse.ok(authService.login(loginRequestDto));
    }

    @PutMapping("/v1/signout")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody SignoutRequest signoutRequest) {
        authService.deactivateUser(authUser, signoutRequest);
        return ApiResponse.ok(null);
    }
}
