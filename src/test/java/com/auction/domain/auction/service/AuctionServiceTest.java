package com.auction.domain.auction.service;

import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.event.publish.AuctionPublisher;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.service.NotificationService;
import com.auction.domain.point.repository.PointRepository;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.enums.UserRole;
import com.auction.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.auction.data.auction.AuctionMockDataUtil.*;
import static com.auction.data.user.UserMockDataUtil.authUser_ROLE_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionPublisher auctionPublisher;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private PointService pointService;
    @Mock
    private PointHistoryService pointHistoryService;
    @Mock
    private DepositService depositService;
    @Mock
    private UserService userService;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Mock
    private KafkaTemplate<String, RefundEvent> kafkaTemplate;
    @InjectMocks
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auctionService, "refundTopic", "refund-point-topic");
    }

    @Test
    public void 연장된_경매() {
        // given
        long auctionId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dataSourceExpiredAt = now.plusDays(1);
        long originExpiredAt = TimeConverter.toLong(now);

        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        Auction auction = mock(Auction.class);
        ReflectionTestUtils.setField(auction, "id", auctionId);

        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getExpireAt()).thenReturn(dataSourceExpiredAt);
        when(auctionEvent.getExpiredAt()).thenReturn(originExpiredAt);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(auctionPublisher).auctionPublisher(eq(auctionEvent), eq(originExpiredAt), eq(TimeConverter.toLong(dataSourceExpiredAt)));
    }

    @Test
    public void 경매_유찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));
        when(auction.getSeller()).thenReturn(new User());

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(notificationService).sendNotification(
                any(User.class), eq(NotificationType.AUCTION),
                eq("경매 아이디 " + auctionId + "이(가) 유찰되었습니다."), anyString()
        );
    }

    @Test
    public void 경매_낙찰() {
        // given
        AuctionEvent auctionEvent = mock(AuctionEvent.class);
        long auctionId = 1L;
        when(auctionEvent.getAuctionId()).thenReturn(auctionId);
        Auction auction = mock(Auction.class);
        when(auctionRepository.findByAuctionId(anyLong())).thenReturn(Optional.of(auction));
        LocalDateTime expireAt = LocalDateTime.now().plusDays(1);
        when(auction.getExpireAt()).thenReturn(expireAt);
        when(auctionEvent.getExpiredAt()).thenReturn(TimeConverter.toLong(expireAt));

        User buyer = new User();
        Long buyerId = 2L;
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForZSet().reverseRange("auction:bid:1", 0L, 0L)).thenReturn(Set.of(buyerId.toString()));

        User seller = new User();
        Long sellerId = 3L;
        ReflectionTestUtils.setField(seller, "id", sellerId);
        int maxPrice = 10000;
        when(auction.getSeller()).thenReturn(seller);
        when(auction.getMaxPrice()).thenReturn(maxPrice);
        when(userService.getUser(buyerId)).thenReturn(buyer);

        Item item = new Item();
        ReflectionTestUtils.setField(item, "name", "itemName");
        when(auction.getItem()).thenReturn(item);

        // when
        auctionService.closeAuction(auctionEvent);

        // then
        verify(pointService).increasePoint(sellerId, maxPrice);
        verify(pointHistoryService).createPointHistory(seller, maxPrice, PaymentType.RECEIVE);
        verify(auction).changeBuyer(buyer);
        verify(depositService).deleteDeposit(buyerId, auctionId);
        verify(notificationService).sendNotification(eq(buyer), eq(NotificationType.AUCTION), anyString(), anyString());
    }

    @Nested
    @DisplayName("입찰 등록 테스트")
    public class CreateBidTest {
        @Test
        @DisplayName("존재하지 않는 경매로 인해 입찰 등록에 실패한다.")
        public void bid_notFoundAuction_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("판매자는 입찰할 수 없어 입찰 등록에 실패한다.")
        public void bid_sellerEqualsBidder_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("마감된 경매라 입찰 등록에 실패한다.")
        public void bid_expiredAuction_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = expiredAuction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가보다 낮아 입찰 등록에 실패한다.")
        public void bid_lowerMaxPrice_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(0);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("입찰가가 최대 입찰가와 같지만 최초 입찰이 아니기에 입찰 등록에 실패한다.")
        public void bid_sameMaxPrice_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(1000);

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("포인트가 부족해 입찰 등록에 실패한다.")
        public void bid_notEnoughPointAmount_failure() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(1);

            // when, then
            assertThrows(ApiException.class,
                    () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
        }

        @Test
        @DisplayName("자동연장 동의 건은 마감 5분 전에 입찰이 들어오면 자동연장 된다.")
        public void bid_agreeAutoExtension_5minBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auctionExpiredBefore5Min();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertNotEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("자동연장 동의 건이어도 마감 5분이 아니라면 자동연장 되지 않는다.")
        public void bid_agreeAutoExtension_not5MinBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("마감 5분 전이더라도 자동연장 동의하지 않았다면 자동연장 되지 않는다.")
        public void bid_notAgreeAutoExtension_5minBeforeClosing() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auctionExpiredBefore5Min();
            ReflectionTestUtils.setField(auction, "isAutoExtension", false);
            LocalDateTime originExpireAt = auction.getExpireAt();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            assertEquals(originExpireAt, auction.getExpireAt());
        }

        @Test
        @DisplayName("이전에 입찰했었다면 보증금을 제외하고 추가 예치된다.")
        public void bid_alreadyBiddingDeposit() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            String deposit = "10000";

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.of(deposit));

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            int gapPrice = bidCreateRequestDto().getPrice() - Integer.parseInt(deposit);
            verify(pointService).decreasePoint(anyLong(), eq(gapPrice));
            verify(pointHistoryService).createPointHistory(any(), eq(gapPrice), any());
        }

        @Test
        @DisplayName("처음 입찰한다면 작성한 입찰가만큼 보증금으로 차감된다.")
        public void bid_firstTimeBiddingDeposit() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(pointService).decreasePoint(anyLong(), eq(bidCreateRequestDto.getPrice()));
            verify(pointHistoryService).createPointHistory(any(), eq(bidCreateRequestDto.getPrice()), any());
        }

        @Test
        @DisplayName("최초 입찰이기에 이전 최고 입찰자는 존재하지 않는다.")
        public void bid_firstTimeBiddingNoRefund() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verifyNoInteractions(auctionPublisher);
        }

        @Test
        @DisplayName("최초 입찰이 아니라면 이전 최고 입찰자에게 보증금을 환불해준다.")
        public void bid_notFirstTimeBiddingRefund() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());
            given(depositService.getDeposit(anyLong(), anyLong())).willReturn(Optional.empty());
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(tuples);

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(auctionPublisher).refundPublisher(any());
        }

        @Test
        @DisplayName("입찰 등록에 성공한다.")
        public void bid_success() {
            // given
            long auctionId = 1L;
            AuthUser authUser = authUser_ROLE_USER();
            ReflectionTestUtils.setField(authUser, "id", 2L);
            BidCreateRequestDto bidCreateRequestDto = bidCreateRequestDto();

            Auction auction = auction();
            Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(ZSetOperations.TypedTuple.of(1L, 1.0));

            given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.zCard(anyString())).willReturn(1L);
            given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

            // when
            auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

            // then
            verify(auctionRepository).save(any());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void 서비스_로직에서_예외_발생시_카프카_롤백() {
        // given
        long auctionId = 1L;
        AuthUser authUser = new AuthUser(1L, "email@email.com", UserRole.USER);
        BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(2000);

        // when
        assertThrows(ApiException.class, () -> auctionService.createBid(authUser, auctionId, bidCreateRequestDto));

        // then : Kafka 메시지가 전송되지 않았는지 검증
        verify(kafkaTemplate, never()).send(anyString(), any(RefundEvent.class));
    }

    @Test
    @Transactional
    @Rollback
    public void 서비스_로직_정상_실행시_카프카_메시지_전송() {
        // given
        long auctionId = 1L;
        AuthUser authUser = new AuthUser(2L, "email@email.com", UserRole.USER);
        BidCreateRequestDto bidCreateRequestDto = new BidCreateRequestDto(2000);

        Auction auction = auction();
        Set<ZSetOperations.TypedTuple<Object>> tuples = Set.of(
                ZSetOperations.TypedTuple.of(3L, 1000.0)
        );

        given(auctionRepository.findByAuctionId(anyLong())).willReturn(Optional.of(auction));
        given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
        given(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(0L))).willReturn(tuples);
        given(pointRepository.findPointByUserId(anyLong())).willReturn(bidCreateRequestDto().getPrice());

        // when
        auctionService.createBid(authUser, auctionId, bidCreateRequestDto);

        // then: Kafka 메시지가 특정 토픽에 전송되었는지 검증
        ArgumentCaptor<RefundEvent> refundEventCaptor = ArgumentCaptor.forClass(RefundEvent.class);
        verify(kafkaTemplate).send(eq("refund-point-topic"), refundEventCaptor.capture());

        // 전송된 RefundEvent 내용 검증
        RefundEvent sentEvent = refundEventCaptor.getValue();
        assertThat(sentEvent.getAuctionId()).isEqualTo(auctionId);
        assertThat(sentEvent.getUserId()).isEqualTo(3L); // 이전 최고 입찰자의 userId
        assertThat(sentEvent.getDeposit()).isEqualTo(1000); // 환불 금액 검증
    }
}