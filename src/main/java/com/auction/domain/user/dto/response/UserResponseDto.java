package com.auction.domain.user.dto.response;

import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponseDto {

    private final String email;
    private final String name;
    private final String nickName;
    private final int zipCode;
    private final String address1;
    private final String address2;
    private final UserRole authority;

    public static UserResponseDto of(User user) {
        return new UserResponseDto(
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
