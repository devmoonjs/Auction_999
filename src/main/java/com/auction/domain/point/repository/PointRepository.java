package com.auction.domain.point.repository;

import com.auction.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointRepository extends JpaRepository<Point, Long> {
    @Query("SELECT pointAmount FROM Point WHERE user.id = ?1")
    int findPointByUserId(long userId);
}
