package com.auction.domain.payment.entity;

import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    private int amount;

    public Payment(String orderId, User user, int amount) {
        this.orderId = orderId;
        this.user = user;
        this.amount = amount;
    }
}
