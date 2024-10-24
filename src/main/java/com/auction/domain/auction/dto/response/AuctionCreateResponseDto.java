package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateResponseDto {
    private Long auctionId;
    private Long sellerId;
    private int minPrice;
    private boolean isAutoExtension;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    private ItemResponseDto item;

    public static AuctionCreateResponseDto from(Auction auction) {
        return new AuctionCreateResponseDto(auction.getId(), auction.getSeller().getId(), auction.getMinPrice(), auction.isAutoExtension(),
                auction.getCreatedAt(), auction.getExpireAt(), ItemResponseDto.from(auction.getItem()));
    }
}
