package com.auction.domain.auction.enums;

import com.sun.jdi.request.InvalidRequestStateException;

import java.util.Arrays;

public enum ItemCategory {
    ELECTRONICS,
    HOME_APPLIANCES,
    FURNITURE,
    CLOTHING,
    BABY_PRODUCTS,
    SPORTS_EQUIPMENT,
    BOOKS,
    BEAUTY,
    TOYS,
    MUSICAL_INSTRUMENTS,
    PET_SUPPLIES,
    AUTOMOTIVE,
    OFFICE_SUPPLIES,
    COLLECTIBLES,
    TOOLS,
    FOOD,
    GIFT_CERTIFICATES,
    HEALTH,
    DIGITAL_CONTENT;

    public static ItemCategory of(String category) {
        return Arrays.stream(ItemCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestStateException("유효하지 않은 카테고리 입니다."));
    }
}
