package com.auction.domain.user.dto.request;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    private String oldPassword;

    @Nullable
    private String newPassword;

    @Nullable
    private String name;

    @Nullable
    private String nickName;

    @Nullable
    private String zipCode;

    @Nullable
    private String address1;

    @Nullable
    private String address2;

    @Nullable
    private String authority;
}
