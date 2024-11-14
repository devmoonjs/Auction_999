package com.auction.domain.auction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "auction_item_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String path;

    @NotNull
    @Column(name = "file_name")
    private String fileName;

    @NotNull
    @Column(name = "origin_name")
    private String originName;

    @NotNull
    private String extension;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
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
