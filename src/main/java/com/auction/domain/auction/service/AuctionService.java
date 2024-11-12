package com.auction.domain.auction.service;

import com.auction.common.annotation.DistributedLock;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.AuctionHistoryDto;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionRankingResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final ItemRepository itemRepository;
    private final PointRepository pointRepository;
    private final AuctionRepository auctionRepository;

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;
    private final UserService userService;
    private final AuctionItemElasticService elasticService;

    private final AuctionPublisher auctionPublisher;
    private final NotificationService notificationService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, RefundEvent> kafkaTemplate;

    @Value("${kafka.topic.refund}")
    private String refundTopic;

    @Value("${notification.related-url.auction}")
    private String relatedAuctionUrl;

    public static final String AUCTION_HISTORY_PREFIX = "auction:bid:";
    public static final String AUCTION_RANKING_PREFIX = "auction:ranking:";

    private Auction getAuctionById(long auctionId) {
        return auctionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION));
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
        elasticService.saveToElastic(ItemDocument.from(savedItem));
        Auction auction = Auction.of(savedItem, User.fromAuthUser(authUser), requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAfter());
        Auction savedAuction = auctionRepository.save(auction);

        auctionPublisher.auctionPublisher(
                AuctionEvent.from(savedAuction),
                TimeConverter.toLong(savedAuction.getExpireAt()),
                new Date().getTime()
        );

        return AuctionCreateResponseDto.from(savedAuction);
    }

    @Transactional(readOnly = true)
    public AuctionResponseDto getAuction(Long auctionId) {
        Auction auctionItem = getAuctionById(auctionId);
        return AuctionResponseDto.from(auctionItem);
    }

    @Transactional(readOnly = true)
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
        elasticService.saveToElastic(ItemDocument.from(savedItem));
        return AuctionResponseDto.from(auction);
    }

    @Transactional
    public String deleteAuctionItem(AuthUser authUser, Long auctionId) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        elasticService.deleteFromElastic(ItemDocument.from(auction.getItem()));
        auctionRepository.delete(auction);
        return "물품이 삭제되었습니다.";
    }

    @DistributedLock(key = "T(java.lang.String).format('Auction%d', #auctionId)")
    public BidCreateResponseDto createBid(AuthUser authUser, long auctionId, BidCreateRequestDto bidCreateRequestDto) {
        User user = User.fromAuthUser(authUser);
        Auction auction = getAuctionById(auctionId);

        if (Objects.equals(auction.getSeller().getId(), user.getId())) {
            throw new ApiException(ErrorStatus._INVALID_BID_REQUEST_USER);
        }

        if (auction.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus._INVALID_BID_CLOSED_AUCTION);
        }

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auction.getId();

        // 입찰가 변환 : ex) 15999 -> 15000
        int bidPrice = (bidCreateRequestDto.getPrice() / 1000) * 1000;
        validBidPrice(bidPrice, auction, auctionHistoryKey);

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

        // 포인트 차감, 보증금 예치
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

        // reids zset 에서 이전 최고 입찰 구매자 보증금 환불
        Set<ZSetOperations.TypedTuple<Object>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(auctionHistoryKey, 0, 0);
        Optional.ofNullable(typedTuples)
                .filter(tuples -> !tuples.isEmpty())
                .ifPresent(tuples -> {
                    for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                        long userId = Long.parseLong(String.valueOf(tuple.getValue()));
                        int price = Objects.requireNonNull(tuple.getScore()).intValue();
                        if (userId != user.getId()) {
                            AuctionHistoryDto auctionHistoryDto = AuctionHistoryDto.of(userId, price);
                            kafkaTemplate.send(refundTopic, RefundEvent.from(auctionId, auctionHistoryDto));
//                            auctionPublisher.refundPublisher(RefundEvent.from(auctionId, auctionHistoryDto));
                        }
                    }
                });

        // redis zset 에 입찰 기록 저장
        redisTemplate.opsForZSet().add(auctionHistoryKey, user.getId().toString(), bidPrice);

        auction.changeMaxPrice(bidPrice);
        auctionRepository.save(auction);

        redisTemplate.opsForZSet().incrementScore(AUCTION_RANKING_PREFIX, String.valueOf(auctionId), 1);

        return BidCreateResponseDto.of(user.getId(), auction);
    }

    @Transactional
    public void closeAuction(AuctionEvent auctionEvent) {
        long auctionId = auctionEvent.getAuctionId();
        Auction auction = getAuctionById(auctionId);

        long originExpiredAt = auctionEvent.getExpiredAt();
        long dataSourceExpiredAt = TimeConverter.toLong(auction.getExpireAt());

        String auctionHistoryKey = AUCTION_HISTORY_PREFIX + auctionId;

        // 마감 시간 수정
        if (dataSourceExpiredAt != originExpiredAt) {
            auctionEvent.changeAuctionExpiredAt(dataSourceExpiredAt);
            auctionPublisher.auctionPublisher(auctionEvent, originExpiredAt, dataSourceExpiredAt);
            return;
        }

        Set<Object> result = redisTemplate.opsForZSet().reverseRange(auctionHistoryKey, 0, 0);
        if (result == null || result.isEmpty()) {
            // 경매 유찰 알림
            notificationService.sendNotification(auction.getSeller(), NotificationType.AUCTION,
                    "경매 아이디 " + auctionId + "이(가) 유찰되었습니다.", relatedAuctionUrl + auctionId);
        } else {
            // 경매 낙찰
            // 판매자 포인트 증가
            pointService.increasePoint(auction.getSeller().getId(), auction.getMaxPrice());
            pointHistoryService.createPointHistory(auction.getSeller(), auction.getMaxPrice(), PaymentType.RECEIVE);

            // 구매자 경매 이력 수정
            String buyerId = (String) result.iterator().next();
            User buyer = userService.getUser(Long.parseLong(buyerId));

            auction.changeBuyer(buyer);

            // 보증금 제거
            depositService.deleteDeposit(buyer.getId(), auctionId);
            log.debug("topBidUser : {}", buyer.getId());

            // 경매 낙찰 알림
            notificationService.sendNotification(buyer,
                    NotificationType.AUCTION,
                    "입찰한 " + auction.getItem().getName() + "이(가) 낙찰되었습니다!",
                    relatedAuctionUrl + auctionId);
        }
        // redis key 삭제
        redisTemplate.delete(auctionHistoryKey);
        redisTemplate.opsForZSet().remove(AUCTION_RANKING_PREFIX, String.valueOf(auctionId));
    }

    @Transactional(readOnly = true)
    public List<AuctionRankingResponseDto> getRankingList() {
        Set<ZSetOperations.TypedTuple<Object>> rankings =
                redisTemplate.opsForZSet().reverseRangeWithScores(AUCTION_RANKING_PREFIX, 0, 9);

        List<AuctionRankingResponseDto> rankingList = new ArrayList<>();

        if (rankings != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> ranking : rankings) {
                long auctionId = Long.parseLong(ranking.getValue().toString());
                Auction auction = getAuctionById(auctionId);
                Integer bidCount = ranking.getScore().intValue();

                rankingList.add(AuctionRankingResponseDto.of(rank++, auctionId, bidCount,
                        auction.getItem().getName(), auction.getMaxPrice(),
                        auction.getExpireAt().toString()));
            }
        }
        return rankingList;
    }

    private void validBidPrice(int bidPrice, Auction auction, String auctionHistoryKey) {
        Long size = redisTemplate.opsForZSet().zCard(auctionHistoryKey);
        if (bidPrice < auction.getMaxPrice() // 입찰가가 현재 최대 입찰가보다 낮거나
                || (bidPrice == auction.getMaxPrice() && size != null && size != 0)) { // 최대 입찰가와 같으면
            throw new ApiException(ErrorStatus._INVALID_LESS_THAN_MAX_PRICE);
        }
    }
}
