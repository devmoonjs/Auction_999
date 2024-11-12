package com.auction.domain.auction.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DltListenerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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"; // Slack Webhook URL

    // DLT 토픽 리스너
    @KafkaListener(topics = "refund-point-topic.DLT", groupId = "refund-dlt-group")
    public void listenToDlt(ConsumerRecord<String, String> record) {
        log.error("DLT 메시지 전송 : {}", record);

        String message = String.format(
                "!DLT 메시지! \nTopic: %s\nKey: %s\nValue: %s\nPartition: %d\nOffset: %d",
                record.topic(),
                record.key(),
                record.value(),
                record.partition(),
                record.offset()
        );

        sendSlackNotification(message);
    }

    private void sendSlackNotification(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = String.format("{\"text\": \"%s\"}", message);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(SLACK_WEBHOOK_URL, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Slack 알림 전송 성공");
        } else {
            log.error("Slack 알림 전송 실패: {}", response.getStatusCode());
        }
    }
}
