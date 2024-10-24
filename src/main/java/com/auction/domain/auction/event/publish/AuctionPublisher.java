package com.auction.domain.auction.event.publish;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionPublisher {
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public void auctionPublisher(Object object, long targetTime1, long targetTime2) {
        try {
            rabbitTemplate.convertAndSend("exchange.auction", "auction",
                    objectMapper.writeValueAsString(object), msg -> {
                        msg.getMessageProperties().setHeader("x-delay", subtractTime(targetTime1, targetTime2));
                        return msg;
                    });
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    public void refundPublisher(Object object) {
        try {
            rabbitTemplate.convertAndSend("exchange.refund", "refund.*", objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus._INVALID_REQUEST);
        }
    }

    private long subtractTime(long millis1, long millis2) {
        return Math.abs(millis1 - millis2);
    }
}
