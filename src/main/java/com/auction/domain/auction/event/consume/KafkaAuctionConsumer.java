package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuctionConsumer {

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;
    private final DepositService depositService;

    @KafkaListener(topics = "refund-point-topic", groupId = "refund-group",
            containerFactory = "refundKafkaListenerContainerFactory")
    public void refundConsumer(RefundEvent refundEvent, Acknowledgment ack) {
            log.info("RefundEvent = {}", refundEvent);
            depositService.deleteDeposit(refundEvent.getUserId(), refundEvent.getAuctionId());
            pointService.increasePoint(refundEvent.getUserId(), refundEvent.getDeposit());
            log.info("Success point refund : {}", refundEvent.getUserId());
            pointHistoryService.createPointHistory(User.fromUserId(refundEvent.getUserId()), refundEvent.getDeposit(), PaymentType.REFUND);

            // 예외 없을 시 커밋
            ack.acknowledge();
    }
}