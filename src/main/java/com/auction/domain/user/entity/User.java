package com.auction.domain.user.entity;

import com.auction.common.entity.AuthUser;
import com.auction.common.entity.TimeStamped;
import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auth.dto.request.SignupRequestDto;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "user")
    private List<AuctionHistory> auctionHistoryList = new ArrayList<>();

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

    public void updateUser(UserUpdateRequestDto requestDto) {
        if (requestDto.getName() != null) this.name = requestDto.getName();
        if (requestDto.getNickName() != null) this.nickName = requestDto.getNickName();
        if (requestDto.getZipCode() != null) this.zipCode = Integer.parseInt(requestDto.getZipCode());
        if (requestDto.getAddress1() != null) this.address1 = requestDto.getAddress1();
        if (requestDto.getAddress2() != null) this.address2 = requestDto.getAddress2();
        if (requestDto.getAuthority() != null) this.authority = UserRole.of(requestDto.getAuthority());
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}
