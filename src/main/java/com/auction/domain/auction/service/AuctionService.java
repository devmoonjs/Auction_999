package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.repository.AuctionHistoryRepository;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {
    private final PointRepository pointRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;

    private AuctionItem getAuctionItem(Long auctionId) {
        return auctionItemRepository.findById(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM));
    }

    public BidCreateResponseDto createBid(AuthUser authUser, long auctionItemId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromAuth(authUser);
        AuctionItem auctionItem = getAuctionItem(auctionItemId);

        // 10시 5분 마감인데 10시 10분에 신청하면 실패
        if (auctionItem.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        if (Objects.equals(auctionItem.getUser().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        if (bidCreateRequestDto.getPrice() < auctionItem.getMaxPrice()) {
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }

        // TODO(Auction) : 15988 => 15000 로 변환 (천원 단위로 결제)

        if (pointRepository.findPointByUserId(user.getId()) < bidCreateRequestDto.getPrice()) {
            throw new ApiException(ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }

        LocalDateTime now = LocalDateTime.now();

        boolean isAutoExtensionNow = auctionItem.getExpireAt().minusMinutes(5L).isBefore(now);
        // 마감 5분 전인지 확인하고, 자동연장
        if (auctionItem.isAutoExtension() && isAutoExtensionNow) {
            auctionItem.changeExpireAt(auctionItem.getExpireAt().plusMinutes(10L));
        }

        auctionItem.changeMaxPrice(bidCreateRequestDto.getPrice());
        auctionItemRepository.save(auctionItem);

        AuctionHistory auctionHistory = AuctionHistory.of(false, bidCreateRequestDto.getPrice(), auctionItem, user);
        auctionHistoryRepository.save(auctionHistory);

        return BidCreateResponseDto.of(user.getId(), auctionItem);
    }
}
