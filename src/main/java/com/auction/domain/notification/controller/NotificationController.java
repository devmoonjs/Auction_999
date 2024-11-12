package com.auction.domain.notification.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.notification.dto.GetNotificationListDto;
import com.auction.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal AuthUser authUser) {
        return notificationService.subscribe(authUser.getId().toString());
    }

    @GetMapping(value = "/subscribe2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMessages(@AuthenticationPrincipal AuthUser authUser) {
        return notificationService.subscribe2(authUser.getId().toString());
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ping() {
        return notificationService.ping();
    }

    @GetMapping
    public ApiResponse<List<GetNotificationListDto>> getNotificationList(@AuthenticationPrincipal AuthUser authUser,
                                                                         @RequestParam(required = false) String type) {
        return ApiResponse.ok(notificationService.getNotificationList(authUser, type));
    }
}
