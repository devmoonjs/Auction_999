package com.auction.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponseDto {

    private final String bearerToken;

    public static LoginResponseDto of(String bearerToken) {
        return new LoginResponseDto(bearerToken);
    }
}
