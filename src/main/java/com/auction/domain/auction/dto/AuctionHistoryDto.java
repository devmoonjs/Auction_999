package com.auction.domain.auction.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionHistoryDto {
    private long userId;
    private int price;

    @QueryProjection
    public AuctionHistoryDto(long userId, int price) {
        this.userId = userId;
        this.price = price;
    }
}
