package com.auction.domain.point.repository;

import com.auction.domain.point.entity.Point;
import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUser(User user);
}
