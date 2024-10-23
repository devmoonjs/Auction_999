package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @Query("SELECT a FROM Auction a JOIN FETCH Item i ON a.item.id = i.id WHERE a.id = ?1")
    Optional<Auction> findById(long auctionId);
    Optional<Auction> findByIdAndSellerId(Long auctionId, Long sellerId);
}
