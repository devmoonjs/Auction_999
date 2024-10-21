package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {
}
