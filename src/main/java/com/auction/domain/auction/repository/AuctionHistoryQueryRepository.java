package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.AuctionHistoryDto;

import java.util.List;

public interface AuctionHistoryQueryRepository {
    List<AuctionHistoryDto> findAuctionHistoryByAuctionId(long auctionId, long userId);
}
