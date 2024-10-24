package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.AuctionHistoryDto;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionHistoryRepository;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {
    private final ItemRepository itemRepository;
    private final PointRepository pointRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;

    private final AuctionPublisher auctionPublisher;

    private Auction getAuction(long auctionId) {
        return auctionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));
    }

    private Auction getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    private Auction getAuctionWithUser(AuthUser authUser, Long auctionId) {
        return auctionRepository.findByIdAndSellerId(auctionId, authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Transactional
    public AuctionCreateResponseDto createAuction(AuthUser authUser, AuctionCreateRequestDto requestDto) {
        Item item = Item.of(requestDto.getItem().getName(),
                requestDto.getItem().getDescription(),
                ItemCategory.of(requestDto.getItem().getCategory()));
        Item savedItem = itemRepository.save(item);
        Auction auction = Auction.of(savedItem, User.fromAuthUser(authUser), requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAt());
        Auction savedAuction = auctionRepository.save(auction);

        auctionPublisher.auctionPublisher(
                AuctionEvent.from(savedAuction),
                TimeConverter.toLong(savedAuction.getExpireAt()),
                TimeConverter.toLong(LocalDateTime.now())
        );

        return AuctionCreateResponseDto.from(savedAuction);
    }

    public AuctionResponseDto getAuction(Long auctionId) {
        Auction auctionItem = getAuctionById(auctionId);
        return AuctionResponseDto.from(auctionItem);
    }

    public Page<AuctionResponseDto> getAuctionList(Pageable pageable) {
        return auctionRepository.findAllCustom(pageable);
    }

    @Transactional
    public AuctionResponseDto updateAuctionItem(AuthUser authUser, Long auctionId, AuctionItemChangeRequestDto requestDto) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        Item item = auction.getItem();

        if (requestDto.getName() != null) {
            item.changeName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            item.changeDescription(requestDto.getDescription());
        }
        if (requestDto.getCategory() != null) {
            item.changeCategory(ItemCategory.of(requestDto.getCategory()));
        }

        Item savedItem = itemRepository.save(item);
        auction.changeItem(savedItem);
        return AuctionResponseDto.from(auction);
    }

    @Transactional
    public String deleteAuctionItem(AuthUser authUser, Long auctionId) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        auctionRepository.delete(auction);
        return "물품이 삭제되었습니다.";
    }

    public Page<AuctionResponseDto> searchAuctionItems(Pageable pageable, String name, String category, String sortBy) {
        return auctionRepository.findByCustomSearch(pageable, name, category, sortBy);
    }

    @Transactional
    public BidCreateResponseDto createBid(AuthUser authUser, long auctionId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromAuthUser(authUser);
        Auction auction = getAuction(auctionId);

        if (Objects.equals(auction.getSeller().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        if (auction.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        // 입찰가 변환 : ex) 15999 -> 15000
        int bidPrice = (bidCreateRequestDto.getPrice() / 1000) * 1000;
        if (bidPrice <= auction.getMaxPrice()) {
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }

        int pointAmount = pointRepository.findPointByUserId(user.getId());
        if (pointAmount < bidPrice) {
            throw new ApiException(ErrorStatus._INVALID_NOT_ENOUGH_POINT);
        }

        LocalDateTime now = LocalDateTime.now();

        boolean isAutoExtensionNow = auction.getExpireAt().minusMinutes(5L).isBefore(now);
        // 마감 5분 전인지 확인하고, 자동연장
        if (auction.isAutoExtension() && isAutoExtensionNow) {
            auction.changeExpireAt(auction.getExpireAt().plusMinutes(10L));
        }

        // 포인트 차감
        depositService.getDeposit(user.getId(), auctionId).ifPresentOrElse(
                (deposit) -> {
                    int prevDeposit = Integer.parseInt(deposit.toString());
                    int gap = bidPrice - prevDeposit;
                    pointService.decreasePoint(user.getId(), gap);
                    pointHistoryService.createPointHistory(user, gap, PaymentType.SPEND);
                },
                () -> {
                    pointService.decreasePoint(user.getId(), bidPrice);
                    pointHistoryService.createPointHistory(user, bidPrice, PaymentType.SPEND);
                }
        );
        depositService.placeDeposit(user.getId(), auctionId, bidPrice);

        AuctionHistory auctionHistory = AuctionHistory.of(false, bidPrice, auction, user);
        auctionHistoryRepository.save(auctionHistory);

        auction.changeMaxPrice(bidPrice);
        auctionRepository.save(auction);

        return BidCreateResponseDto.of(user.getId(), auction);
    }

    @Transactional
    public void closeAuction(AuctionEvent auctionEvent) {
        long auctionId = auctionEvent.getAuctionId();
        Auction auction = getAuction(auctionId);

        long originExpiredAt = auctionEvent.getExpiredAt();
        long dataSourceExpiredAt = TimeConverter.toLong(auction.getExpireAt());
        // 마감 시간 수정
        if (dataSourceExpiredAt != originExpiredAt) {
            auctionEvent.changeAuctionExpiredAt(dataSourceExpiredAt);
            auctionPublisher.auctionPublisher(auctionEvent, originExpiredAt, dataSourceExpiredAt);
            return;
        }

        auctionHistoryRepository.getLastBidAuctionHistory(auctionId).ifPresentOrElse(
                auctionHistory -> {
                    // 경매 낙찰
                    // 구매자 경매 이력 수정
                    auctionHistory.changeIsSold(true);
                    auctionHistoryRepository.save(auctionHistory);

                    auction.changeBuyer(auctionHistory.getUser());
                    auctionRepository.save(auction);

                    // TODO(Auction) : 경매 낙찰로 인한 알림 (V2)

                    // TODO(Auction) : 경매 패찰로 인한 알림 (V2)
                    List<AuctionHistoryDto> list = auctionHistoryRepository.findAuctionHistoryByAuctionId(auctionId, auctionHistory.getUser().getId());

                    for (AuctionHistoryDto auctionHistoryDto : list) {
                        auctionPublisher.refundPublisher(RefundEvent.from(auctionHistoryDto));
                    }
                },
                () -> {
                    // 경매 유찰
                    // TODO(Auction) : 경매 유찰로 인한 알림 (V2)
                }
        );
    }
}
