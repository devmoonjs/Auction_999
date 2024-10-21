package com.auction.domain.auth.dto.response;

import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignupResponseDto {

    private final String email;
    private final String name;
    private final String nickName;
    private final int zipCode;
    private final String address1;
    private final String address2;
    private final UserRole authority;

    public static SignupResponseDto of(User user) {
        return new SignupResponseDto(
                user.getEmail(),
                user.getName(),
                user.getNickName(),
                user.getZipCode(),
                user.getAddress1(),
                user.getAddress2(),
                user.getAuthority()
                );
    }
}
