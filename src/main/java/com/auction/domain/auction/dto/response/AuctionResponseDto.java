package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponseDto {
    private Long auctionId;
    private Long sellerId;
    private Long buyerId;
    private int minPrice;
    private int maxPrice;
    private boolean isSold;
    private boolean isAutoExtension;
    private boolean isAgreeEvent;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private ItemResponseDto item;

    public static AuctionResponseDto from(Auction auction) {
        return new AuctionResponseDto(auction.getId(), auction.getSeller().getId(), auction.getBuyerId(),
                auction.getMinPrice(), auction.getMaxPrice(), auction.isSold(), auction.isAutoExtension(),
                auction.isAgreeEvent(), auction.getCreatedAt(), auction.getExpireAt(), ItemResponseDto.from(auction.getItem()));
    }
}
