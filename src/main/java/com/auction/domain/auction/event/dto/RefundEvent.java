package com.auction.domain.auction.event.dto;

import com.auction.domain.auction.dto.AuctionHistoryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RefundEvent implements Serializable {
    private long userId;
    private int deposit;

    public static RefundEvent from(AuctionHistoryDto auctionHistorydto) {
        return new RefundEvent(
                auctionHistorydto.getUserId(),
                auctionHistorydto.getPrice()
        );
    }
}
