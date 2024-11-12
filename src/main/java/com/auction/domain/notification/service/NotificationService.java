package com.auction.domain.notification.service;

import com.auction.common.entity.AuthUser;
import com.auction.domain.notification.dto.GetNotificationListDto;
import com.auction.domain.notification.dto.NotificationDto;
import com.auction.domain.notification.entity.Notification;
import com.auction.domain.notification.enums.NotificationType;
import com.auction.domain.notification.repository.NotificationRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final RedisMessageService redisMessageService;

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    // MVC
    public SseEmitter subscribe(String userId) {
        SseEmitter sseEmitter = new SseEmitter(5000L);      // 테스트 용 (5초 후 자동 종료)
        sseEmitterService.createEmitter(userId);
        sseEmitterService.send("EventStream Created.", userId, sseEmitter); // send dummy

        redisMessageService.subscribe(userId); // redis 구독

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> {
            sseEmitterService.deleteEmitter(userId);
            redisMessageService.removeSubscribe(userId); // 구독한 채널 삭제
        });
        return sseEmitter;
    }

    // webflux 사용
    public Flux<ServerSentEvent<String>> subscribe2(String userId) {
        return redisTemplate.listenTo(new ChannelTopic(userId))
                .map(message -> ServerSentEvent.<String>builder()
                        .data(message.getMessage())
                        .build())
                .take(Duration.ofSeconds(5));   // 테스트 용
    }

    public Flux<ServerSentEvent<String>> ping() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> ServerSentEvent.<String>builder()
                        .event("ping")
                        .data(String.valueOf(Instant.now().toEpochMilli()))
                        .build());
    }

    @Transactional
    public void sendNotification(User receiver, NotificationType notificationType, String content, String relatedUrl) {
        Notification notification = notificationRepository.save(
                Notification.of(receiver, content, relatedUrl, notificationType));

        // redis 이벤트 발행
        redisMessageService.publish(receiver.getId().toString(), NotificationDto.from(notification));
    }

    @Transactional
    public List<GetNotificationListDto> getNotificationList(AuthUser authUser, String type) {
        return notificationRepository.getNotificationListByUserIdAndType(authUser.getId(), type);
    }
}
