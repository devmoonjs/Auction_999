package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class BidCreateResponseDto {
    private final long auctionId;
    private final long userId;
    private final String name;
    private final ItemCategory category;
    private final String description;
    private final LocalDateTime expireAt;
    private final int minPrice;
    private final int maxPrice;
    private final boolean isAutoExtension;

    public static BidCreateResponseDto of(long userId, Auction auction) {
        return new BidCreateResponseDto(
                auction.getId(),
                userId,
                auction.getItem().getName(),
                auction.getItem().getCategory(),
                auction.getItem().getDescription(),
                auction.getExpireAt(),
                auction.getMinPrice(),
                auction.getMaxPrice(),
                auction.isAutoExtension()
        );
    }
}