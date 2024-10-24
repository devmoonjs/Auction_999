package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.AuctionEvent;
import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionConsumer {

    private final ObjectMapper objectMapper;
    private final AuctionService auctionService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    @RabbitListener(queues = "auction.queue")
    public void auctionConsumer(String message) {
        try {
            log.info("AuctionEvent : {}", message);
            AuctionEvent auctionEvent = objectMapper.readValue(message, AuctionEvent.class);
            auctionService.closeAuction(auctionEvent);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @RabbitListener(queues = "refund.queue")
    public void refundConsumer(String message) {
        try {
            log.info("RefundEvent : {}", message);
            RefundEvent refundEvent = objectMapper.readValue(message, RefundEvent.class);
            pointService.increasePoint(refundEvent.getUserId(), refundEvent.getDeposit());
            pointHistoryService.createPointHistory(User.fromUserId(refundEvent.getUserId()), refundEvent.getDeposit(), PaymentType.REFUND);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}