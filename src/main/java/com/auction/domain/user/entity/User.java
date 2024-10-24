package com.auction.domain.user.entity;

import com.auction.common.entity.AuthUser;
import com.auction.common.entity.TimeStamped;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class User extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private String name;
    private String nickName;
    private int zipCode;
    private String address1;
    private String address2;

    @Enumerated(EnumType.STRING)
    private UserRole authority;

    private boolean activate;
    private LocalDateTime deletedAt;

    public User(String encodedPassword, SignupRequestDto requestDto) {
        this.email = requestDto.getEmail();
        this.password = encodedPassword;
        this.name = requestDto.getName();
        this.nickName = requestDto.getNickName();
        this.zipCode = requestDto.getZipCode();
        this.address1 = requestDto.getAddress1();
        this.address2 = requestDto.getAddress2();
        this.authority = UserRole.of(requestDto.getAuthority());
        this.activate = true;
    }

    private User(long id) {
        this.id = id;
    }

    private User(long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.authority = userRole;
    }

    public void changeDeactivate() {
        this.activate = false;
        this.deletedAt = LocalDateTime.now();
    }

    private User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.authority = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return new User(authUser.getId(), authUser.getEmail(), authUser.getUserRole());
    }

    public static User fromUserId(long userId) {
        return new User(userId);
    }
}
