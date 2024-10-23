package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
