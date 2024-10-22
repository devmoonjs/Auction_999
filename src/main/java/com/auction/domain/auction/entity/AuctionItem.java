package com.auction.domain.auction.entity;

import com.auction.common.entity.TimeStamped;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class AuctionItem extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    private String content;

    @Column(nullable = false)
    private int minPrice;
    private int maxPrice;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireAt;

    private boolean isAutoExtension;

    private AuctionItem(User user, String name, String content, int minPrice, int maxPrice, ItemCategory category, LocalDateTime expireAt, boolean isAutoExtension) {
        this.user = user;
        this.name = name;
        this.content = content;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.category = category;
        this.expireAt = expireAt;
        this.isAutoExtension = isAutoExtension;
    }

    public static AuctionItem of(User user, String name, String content, int minPrice, int maxPrice, ItemCategory category, LocalDateTime expireAt, boolean isAutoExtension) {
        return new AuctionItem(user, name, content, minPrice, maxPrice, category, expireAt, isAutoExtension);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public void changeCategory(ItemCategory category) {
        this.category = category;
    }

    public void changeAutoExtension(boolean isAutoExtension) {
        this.isAutoExtension = isAutoExtension;
    }

    public void changeMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void changeExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
