package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.AuctionEvent;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionHistoryRepository;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {
    private final PointRepository pointRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;

    private final PointService pointService;

    private final AuctionPublisher auctionPublisher;

    private Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM));
    }

    public BidCreateResponseDto createBid(AuthUser authUser, long auctionId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromAuth(authUser);
        Auction auction = getAuction(auctionId);

        // 10시 5분 마감인데 10시 10분에 신청하면 실패
        if (auction.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        if (Objects.equals(auction.getSeller().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        if (bidCreateRequestDto.getPrice() < auction.getMaxPrice()) {
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }

        // TODO(Auction) : 15988 => 15000 로 변환 (천원 단위로 결제)

        if (pointRepository.findPointByUserId(user.getId()) < bidCreateRequestDto.getPrice()) {
            throw new ApiException(ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }

        LocalDateTime now = LocalDateTime.now();

        boolean isAutoExtensionNow = auction.getExpireAt().minusMinutes(5L).isBefore(now);
        // 마감 5분 전인지 확인하고, 자동연장
        if (auction.isAutoExtension() && isAutoExtensionNow) {
            auction.changeExpireAt(auction.getExpireAt().plusMinutes(10L));
        }

        auction.changeMaxPrice(bidCreateRequestDto.getPrice());
        auctionRepository.save(auction);

        AuctionHistory auctionHistory = AuctionHistory.of(false, bidCreateRequestDto.getPrice(), auction, user);
        auctionHistoryRepository.save(auctionHistory);

        return BidCreateResponseDto.of(user.getId(), auction);
    }

    public void closeAuction(AuctionEvent auctionEvent) {
        long auctionId = auctionEvent.getAuctionId();
        Auction auction = getAuction(auctionId);
        Optional<AuctionHistory> optionalLastBidHistory = auctionHistoryRepository.getLastBidAuctionHistory(auctionId);

        long originExpiredAt = auctionEvent.getExpiredAt();
        long dataSourceExpiredAt = TimeConverter.toLong(auction.getExpireAt());
        // 마감 시간 수정
        if (dataSourceExpiredAt != originExpiredAt) {
            auctionEvent.changeAuctionExpiredAt(dataSourceExpiredAt);
            auctionPublisher.auctionProcessPublisher(auctionEvent, originExpiredAt, dataSourceExpiredAt);
            return;
        }

        // 경매 유찰
        if (optionalLastBidHistory.isEmpty()) {
            // TODO(Auction) : 경매 유찰로 인한 알림
        }
        // 경매 낙찰
        else {
            AuctionHistory lastBidHistory = optionalLastBidHistory.get();

            // 구매자 경매 이력 수정
            lastBidHistory.changeIsSold(true);
            auctionHistoryRepository.save(lastBidHistory);

            // 포인트 차감
            pointService.decreasePoint(lastBidHistory.getUser().getId(), lastBidHistory.getPrice());

            // TODO(Auction) : 경매 낙찰로 인한 알림
        }
    }

    private long subtractTime(long millis1, long millis2) {
        return Math.abs(millis1 - millis2);
    }
}
