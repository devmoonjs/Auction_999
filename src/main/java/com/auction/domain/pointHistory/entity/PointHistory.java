package com.auction.domain.pointHistory.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class PointHistory extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    private int price;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public PointHistory(User user, int price, PaymentType paymentType) {
        this.user = user;
        this.price = price;
        this.paymentType = paymentType;
    }
}
