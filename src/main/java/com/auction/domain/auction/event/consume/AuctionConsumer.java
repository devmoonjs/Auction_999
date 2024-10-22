package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.dto.AuctionEvent;
import com.auction.domain.auction.service.AuctionService;
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

    @RabbitListener(queues = "auction.process.queue")
    public void auctionProcessConsumer(String message) {
        try {
            log.info("AuctionEvent : {}", message);
            AuctionEvent auctionEvent = objectMapper.readValue(message, AuctionEvent.class);
            auctionService.closeAuction(auctionEvent);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}