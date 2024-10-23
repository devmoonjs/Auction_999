package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.Optional;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {
    Optional<AuctionHistory> findTopByAuctionIdOrderByCreatedAtDesc(long auctionId);

    default boolean existsAuctionHistory(long auctionId) {
        return findTopByAuctionIdOrderByCreatedAtDesc(auctionId).isPresent();
    }

    default Optional<AuctionHistory> getLastBidAuctionHistory(long auctionId) {
        return findTopByAuctionIdOrderByCreatedAtDesc(auctionId);
    }
}
