package com.auction.domain.user.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.response.AuctionItemResponseDto;
import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.repository.AuctionHistoryRepository;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.auth.dto.request.SignoutRequest;
import com.auction.domain.auth.service.AuthService;
import com.auction.domain.user.dto.request.UserUpdateRequestDto;
import com.auction.domain.user.dto.response.UserResponseDto;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuctionItemRepository auctionItemRepository;

    // 유저 soft-delete 처리
    @Transactional
    public void deactivateUser(AuthUser authUser, SignoutRequest signoutRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_USER)
        );

        authService.isDeactivateUser(user);
        authService.checkPassword(signoutRequest.getPassword(), user.getPassword());
        user.changeDeactivate();
    }

    // 유저 정보 수정
    @Transactional
    public UserResponseDto updateUser(AuthUser authUser, UserUpdateRequestDto userUpdateRequest) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_USER)
        );

        authService.isDeactivateUser(user);
        authService.checkPassword(userUpdateRequest.getOldPassword(), user.getPassword());

        if (userUpdateRequest.getNewPassword() != null) {
            String newPassword = passwordEncoder.encode(userUpdateRequest.getNewPassword());
            user.changePassword(newPassword);
        }

        user.updateUser(userUpdateRequest);

        return UserResponseDto.of(user);
    }

    // 판매 내역 리스트 반환
    public List<AuctionItemResponseDto> getSales(AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        List<AuctionItem> itemList = auctionItemRepository.findByUser(user);

        return itemList.stream()
                .map(AuctionItemResponseDto::from)
                .collect(Collectors.toList());
    }


    // 옥션 히스토리에서 userId 가 같고 is_sold 가 true 인 값 -> 본인 구매 내역
    public List<AuctionItemResponseDto> getPurchases(AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        return user.getAuctionHistoryList().stream()
                .filter(AuctionHistory::isSold)
                .map(auctionHistory -> AuctionItemResponseDto.from(auctionHistory.getAuctionItem()))
                .collect(Collectors.toList());
    }
}
