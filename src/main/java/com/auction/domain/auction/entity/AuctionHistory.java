package com.auction.domain.auction.entity;

import com.auction.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean isSold;
    @NotNull
    private int price;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "auction_item_id")
    private AuctionItem auctionItem;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private AuctionHistory(boolean isSold, int price, AuctionItem auctionItem, User user) {
        this.isSold = isSold;
        this.price = price;
        this.auctionItem = auctionItem;
        this.user = user;
    }

    public static AuctionHistory of(boolean isSold, int price, AuctionItem auctionItem, User user) {
        return new AuctionHistory(isSold, price, auctionItem, user);
    }
}
