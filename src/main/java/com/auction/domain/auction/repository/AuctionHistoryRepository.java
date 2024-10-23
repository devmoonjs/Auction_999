package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long>, AuctionQueryRepository {
}
