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
    @JoinColumn(name = "itemId", nullable = false)
    private Item item;

    private AuctionItemImage(String path, String fileName, String originName, String extension, Item item) {
        this.path = path;
        this.fileName = fileName;
        this.originName = originName;
        this.extension = extension;
        this.item = item;
    }

    public static AuctionItemImage of(String path, String fileName, String originName, String extension, Item item) {
        return new AuctionItemImage(path, fileName, originName, extension, item);
    }
}
