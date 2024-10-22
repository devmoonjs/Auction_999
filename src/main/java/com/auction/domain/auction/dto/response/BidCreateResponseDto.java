package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class BidCreateResponseDto {
    private final long auctionItemId;
    private final long userId;
    private final String name;
    private final ItemCategory category;
    private final String content;
    private final LocalDateTime expireAt;
    private final int minPrice;
    private final int maxPrice;
    private final boolean isAutoExtension;

    public static BidCreateResponseDto of(long userId, AuctionItem auctionItem) {
        return new BidCreateResponseDto(
                auctionItem.getId(),
                userId,
                auctionItem.getName(),
                auctionItem.getCategory(),
                auctionItem.getContent(),
                auctionItem.getExpireAt(),
                auctionItem.getMinPrice(),
                auctionItem.getMaxPrice(),
                auctionItem.isAutoExtension()
        );
    }
}