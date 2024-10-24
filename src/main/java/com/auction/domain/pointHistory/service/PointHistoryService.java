package com.auction.domain.pointHistory.service;

import com.auction.domain.pointHistory.entity.PointHistory;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.repository.PointHistoryRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void createPointHistory(User user, int price, PaymentType paymentType) {
        PointHistory pointHistory = new PointHistory(user, price, paymentType);
        pointHistoryRepository.save(pointHistory);
    }

}
