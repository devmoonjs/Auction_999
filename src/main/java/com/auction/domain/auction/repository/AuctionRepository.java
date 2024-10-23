package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findByIdAndSellerId(Long auctionId, Long sellerId);
}
