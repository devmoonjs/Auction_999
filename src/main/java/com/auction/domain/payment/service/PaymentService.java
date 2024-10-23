package com.auction.domain.payment.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.payment.entity.Payment;
import com.auction.domain.payment.repository.PaymentRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPayment(String orderId, User user, int amount) {
        Payment payment = new Payment(orderId, user, amount);
        paymentRepository.save(payment);
    }

    public Payment getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new ApiException(ErrorStatus._INVALID_REQUEST));
    }
}
