package com.auction.domain.auction.dto;

import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.entity.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuctionEvent implements Serializable {
    private long auctionId;
    private long userId;
    private long expiredAt;

    public static AuctionEvent from(Auction auctionItem) {
        return new AuctionEvent(
                auctionItem.getId(),
                auctionItem.getSeller().getId(),
                TimeConverter.toLong(auctionItem.getExpireAt())
        );
    }

    public void changeAuctionExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }
}
