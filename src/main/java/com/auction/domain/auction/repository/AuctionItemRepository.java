package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    Optional<AuctionItem> findByIdAndUserId(Long auctionId, Long userId);
}
