package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ItemResponseDto {
    private Long itemId;
    private String name;
    private String description;
    private ItemCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ItemResponseDto from(final Item item) {

    }
}
