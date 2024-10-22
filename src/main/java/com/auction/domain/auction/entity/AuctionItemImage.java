package com.auction.domain.auction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class AuctionItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;
    private String fileName;
    private String originName;
    private String extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auctionItemId", nullable = false)
    private AuctionItem auctionItem;

    private AuctionItemImage(String path, String fileName, String originName, String extension, AuctionItem auctionItem) {
        this.path = path;
        this.fileName = fileName;
        this.originName = originName;
        this.extension = extension;
        this.auctionItem = auctionItem;
    }

    public static AuctionItemImage of(String path, String fileName, String originName, String extension, AuctionItem auctionItem) {
        return new AuctionItemImage(path, fileName, originName, extension, auctionItem);
    }
}
