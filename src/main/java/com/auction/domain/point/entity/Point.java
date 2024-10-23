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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
