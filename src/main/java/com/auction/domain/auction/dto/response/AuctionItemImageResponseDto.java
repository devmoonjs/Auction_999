package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.AuctionItemImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuctionItemImageResponseDto {
    private Long id;
    private String path;
    private String fileName;
    private String originName;
    private String extension;

    public static AuctionItemImageResponseDto from(AuctionItemImage auctionItemImage) {
        return new AuctionItemImageResponseDto(
                auctionItemImage.getId(),
                auctionItemImage.getPath(),
                auctionItemImage.getFileName(),
                auctionItemImage.getOriginName(),
                auctionItemImage.getExtension()
        );
    }
}
