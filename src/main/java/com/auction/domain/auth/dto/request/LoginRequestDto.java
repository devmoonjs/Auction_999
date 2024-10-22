package com.auction.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {

    private String email;
    private String password;
}
