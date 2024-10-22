package com.auction.domain.auction.dto;

import com.auction.common.utils.TimeConverter;
import com.auction.domain.auction.entity.AuctionItem;
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
    private long auctionItemId;
    private long userId;
    private long expiredAt;

    public static AuctionEvent from(AuctionItem auctionItem) {
        return new AuctionEvent(
                auctionItem.getId(),
                auctionItem.getUser().getId(),
                TimeConverter.toLong(auctionItem.getExpireAt())
        );
    }

    public void changeAuctionExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }
}
