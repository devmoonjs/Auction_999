package com.auction.domain.point.controller;

import com.auction.common.entity.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointController {
    @Value("${payment.secret.key}")
    private String API_SECRET_KEY;
    @Value("${payment.client.key}")
    private String CLIENT_KEY;

    @GetMapping("/buy")
    public String getPaymentPage(@AuthenticationPrincipal AuthUser authUser,
                                 @RequestParam int amount,
                                 Model model) {
        // todo user 정보 가져오는 로직

        model.addAttribute("userId", authUser.getId());
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("amount", amount);

        return "payment/checkout";
    }
}
