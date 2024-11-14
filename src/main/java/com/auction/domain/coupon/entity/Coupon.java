package com.auction.domain.coupon.entity;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.coupon.dto.request.CouponCreateRequestDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "expire_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expireAt;

    private Integer amount;

    @NotNull
    private String name;

    @NotNull
    @Column(name = "discount_rate")
    private int discountRate;

    // 낙관적 락 사용시 추가
//    @Version
//    private int version;

    private Coupon(LocalDate expireAt, Integer amount, String name, int discountRate) {
        this.expireAt = expireAt;
        this.amount = amount;
        this.name = name;
        this.discountRate = discountRate;
    }

    public static Coupon from(CouponCreateRequestDto requestDto) {
        return new Coupon(requestDto.getExpiredAt(), requestDto.getAmount(),
                requestDto.getName(), requestDto.getDiscountRate());
    }

    public void decrementAmount() {
        if (amount == null) return;

        if (amount <= 0) {
            throw new ApiException(ErrorStatus._SOLD_OUT_COUPON);
        }

        amount--;
    }
}
