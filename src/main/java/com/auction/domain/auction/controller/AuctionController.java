package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    /**
     * 입찰 등록
     *
     * @param authUser
     * @param auctionItemId       경매 물품 식별자
     * @param bidCreateRequestDto
     * @return
     */
    @PostMapping("/v1/auctions/{auctionItemId}/bid")
    public ApiResponse<BidCreateResponseDto> createBid(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("auctionItemId") Long auctionItemId,
            @Valid @RequestBody BidCreateRequestDto bidCreateRequestDto
    ) {
        return ApiResponse.ok(auctionService.createBid(authUser, auctionItemId, bidCreateRequestDto));
    }
}
