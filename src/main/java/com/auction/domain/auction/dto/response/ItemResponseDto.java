package com.auction.domain.auction.dto.response;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponseDto {
    private Long itemId;
    private String name;
    private String description;
    private ItemCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ItemResponseDto from(Item item) {
        return new ItemResponseDto(item.getId(), item.getName(), item.getDescription(), item.getCategory(), item.getCreatedAt(), item.getModifiedAt());
    }
}
