package com.auction.domain.user.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.response.AuctionItemResponseDto;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.dto.response.UserResponseDto;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PutMapping("/v1/users")
    public ApiResponse<Void> deactivateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody SignoutRequest signoutRequest) {

        userService.deactivateUser(authUser, signoutRequest);
        return ApiResponse.ok(null);
    }

    @PostMapping("/v1/users")
    public ApiResponse<UserResponseDto> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserUpdateRequestDto userUpdateRequest) {

        return ApiResponse.ok(userService.updateUser(authUser, userUpdateRequest));
    }

    @GetMapping("/v1/users/mypage/sales")
    public ApiResponse<List<AuctionItemResponseDto>> getSales(
        @AuthenticationPrincipal AuthUser authUser) {

        return ApiResponse.ok(userService.getSales(authUser));
    }

    @GetMapping("/v1/users/mypage/purchases")
    public ApiResponse<List<AuctionItemResponseDto>> getPurchases(
            @AuthenticationPrincipal AuthUser authUser) {

        return ApiResponse.ok(userService.getPurchases(authUser));
    }
}
