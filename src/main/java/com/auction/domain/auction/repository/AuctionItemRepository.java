package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    Optional<AuctionItem> findByIdAndUserId(Long auctionId, Long userId);

    List<AuctionItem> findByUser(User user);
}
