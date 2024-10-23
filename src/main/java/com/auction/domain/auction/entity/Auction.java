package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Auction extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private User seller;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId")
    private User buyer;

    @Column(nullable = false)
    private int minPrice;
    private int maxPrice;

    private boolean isSold;
    private boolean isAutoExtension;
    private boolean isAgreeEvent;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireAt;

    private Auction(Item item, User seller, int minPrice, boolean isAutoExtension, LocalDateTime expireAt) {
        this.item = item;
        this.seller = seller;
        this.minPrice = minPrice;
        this.maxPrice = minPrice;
        this.isAutoExtension = isAutoExtension;
        this.expireAt = expireAt;
    }
    public static Auction of(Item item, User seller, int minPrice, boolean isAutoExtension, LocalDateTime expireAt) {
        return new Auction(item, seller, minPrice, isAutoExtension, expireAt);
    }
    public void changeItem(Item item) {
        this.item = item;
    }
    public Long getBuyerId() {
        return buyer != null ? buyer.getId() : null;
    }
}
