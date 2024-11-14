package com.auction.domain.coupon.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupon_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUser extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime usedAt;

    @Column(name = "point_history_Id")
    private Long pointHistoryId;

    @NotNull
    private boolean isAvailable = true;

    public void useCoupon(long pointHistoryId) {
        this.pointHistoryId = pointHistoryId;
        this.isAvailable = false;
        this.usedAt = LocalDateTime.now();
    }

    private CouponUser(Coupon coupon, User user) {
        this.coupon = coupon;
        this.user = user;
    }

    public static CouponUser from(Coupon coupon, User user) {
        return new CouponUser(coupon, user);
    }
}
