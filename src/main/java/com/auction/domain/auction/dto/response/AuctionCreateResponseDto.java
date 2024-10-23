package com.auction.domain.auction.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionCreateResponseDto {
    private Long auctionId;
    private Long sellerId;
    private int minPrice;
    private boolean isAutoExtension;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private ItemResponseDto item;
}
