package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private String content;
    private int minPrice;
    private int maxPrice;
    private ItemCategory category;
    private LocalDateTime expireAt;
    private boolean isAutoExtension;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;

    public static AuctionItemResponseDto from(AuctionItem auctionItem) {
        return new AuctionItemResponseDto(
                auctionItem.getId(), auctionItem.getUser().getId(), auctionItem.getName(), auctionItem.getContent(),
                auctionItem.getMinPrice(), auctionItem.getMaxPrice(), auctionItem.getCategory(), auctionItem.getExpireAt(),
                auctionItem.isAutoExtension(), auctionItem.getCreatedAt(), auctionItem.getModifiedAt()
        );
    }
}
