package com.auction.domain.point.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.point.entity.Point;
import com.auction.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    private Point getPoint(long userId) {
        return pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus._INVALID_REQUEST));
    }

    public void decreasePoint(long userId, int price) {
        Point point = getPoint(userId);
        point.changePoint(point.getPointAmount() - price);
        pointRepository.save(point);
    }

    public void increasePoint(long userId, int price) {
        Point point = getPoint(userId);
        point.changePoint(point.getPointAmount() + price);
        pointRepository.save(point);
    }
}
