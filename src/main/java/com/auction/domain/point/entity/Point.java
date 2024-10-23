package com.auction.domain.point.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point")
@NoArgsConstructor
public class Point extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int pointAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public void addPoint(int amount) {
        this.pointAmount += amount;
    }

    public Point(int pointAmount, User user) {
        this.pointAmount = pointAmount;
        this.user = user;
    }
}
