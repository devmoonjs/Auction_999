package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {
    Optional<AuctionHistory> findTopByAuctionItemIdOrderByCreatedAtDesc(Long auctionItemId);

    default Optional<AuctionHistory> getLastBidAuctionHistory(Long auctionItemId) {
        return findTopByAuctionItemIdOrderByCreatedAtDesc(auctionItemId);
    }
}
