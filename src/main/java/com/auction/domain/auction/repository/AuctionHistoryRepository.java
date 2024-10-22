package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.AuctionHistory;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {

    List<AuctionHistory> findByUserId(long l);
}
