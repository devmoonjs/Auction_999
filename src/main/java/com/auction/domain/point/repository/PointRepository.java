package com.auction.domain.point.repository;

import com.auction.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
