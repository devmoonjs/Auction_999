package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AuctionItemImageRepository extends JpaRepository<AuctionItemImage, Long> {
}
