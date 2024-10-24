package com.auction.domain.point.repository;

import com.auction.domain.point.entity.Point;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUser(User user);

    @Query("SELECT pointAmount FROM Point WHERE user.id = ?1")
    int findPointByUserId(long userId);

    Optional<Point> findByUserId(long userId);
}
