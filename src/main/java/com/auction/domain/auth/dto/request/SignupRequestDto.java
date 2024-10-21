package com.auction.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수 입력사항입니다.")
    @Email(message = "이메일 형식을 맞춰주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력사항입니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력사항입니다.")
    private String name;

    private String nickName;
    private int zipCode;
    private String address1;
    private String address2;
    private String authority;
}
