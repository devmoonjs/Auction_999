package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
}
