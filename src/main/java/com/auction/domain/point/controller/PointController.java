package com.auction.domain.point.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.payment.service.PaymentService;
import com.auction.domain.point.dto.request.ConvertRequestDto;
import com.auction.domain.point.dto.response.ChargeResponseDto;
import com.auction.domain.point.dto.response.ConvertResponseDto;
import com.auction.domain.point.service.PointService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointController {
    @Value("${payment.client.key}")
    private String CLIENT_KEY;
    private final PaymentService paymentService;
    private final PointService pointService;

    @GetMapping("/buy")
    public String getPaymentPage(@AuthenticationPrincipal AuthUser authUser,
                                 @RequestParam int amount,
                                 Model model) {
        String orderId = UUID.randomUUID().toString().substring(0, 10);

        model.addAttribute("userId", authUser.getId());
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("amount", amount);
        model.addAttribute("orderId", orderId);

        paymentService.createPayment(orderId, User.fromAuthUser(authUser), amount);
        return "payment/checkout";
    }

    // 결제 인증 성공시 승인
    @PostMapping("/buy/confirm")
    @ResponseBody
    public ApiResponse<ChargeResponseDto> confirmPayment(@RequestBody String jsonBody) throws IOException {
        ChargeResponseDto chargeResponseDto = pointService.confirmPayment(jsonBody);

        return ApiResponse.ok(chargeResponseDto);
    }

    @PostMapping("/to-cash")
    @ResponseBody
    public ApiResponse<ConvertResponseDto> convertPoint(@AuthenticationPrincipal AuthUser authUser,
                                                        @RequestBody ConvertRequestDto convertRequestDto) {
        return ApiResponse.ok(pointService.convertPoint(authUser, convertRequestDto));
    }
}
