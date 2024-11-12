package com.auction.domain.auction.event.consume;

import com.auction.domain.auction.event.dto.RefundEvent;
import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.service.PointHistoryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.ConnectException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"refund-point-topic", "refund-point-topic.DLT"})
class KafkaAuctionConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, RefundEvent> kafkaTemplate;

    @Mock
    private PointService pointService;

    @Mock
    private PointHistoryService pointHistoryService;

    @Mock
    private DepositService depositService;

    @InjectMocks
    private KafkaAuctionConsumer kafkaAuctionConsumer;

    @Test
    void dlt전송테스트() {
        RefundEvent refundEvent = new RefundEvent(1L, 1L, 1000);

        // pointService 호출 시 RuntimeException으로 감싸서 ConnectException을 던지도록 설정
        doThrow(new RuntimeException(new ConnectException("Point service is unavailable")))
                .when(pointService).increasePoint(anyLong(), anyInt());

        // refundConsumer 호출
        kafkaAuctionConsumer.refundConsumer(refundEvent, any(Acknowledgment.class));

        // DLT로 메시지가 전송되었는지 확인
        verify(kafkaTemplate).send(anyString(), any(), any());
    }
}
