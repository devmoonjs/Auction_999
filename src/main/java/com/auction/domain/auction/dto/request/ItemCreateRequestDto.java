package com.auction.domain.auction.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemCreateRequestDto {
    private String name;
    private String description;
    private String category;
}
