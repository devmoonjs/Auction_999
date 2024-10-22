package com.auction.domain.auction.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class AuctionItemChangeRequestDto {
    private String name;
    private String content;
    @Min(value = 1000, message = "최소 금액은 1000원 입니다.")
    private Integer minPrice;
    private String category;
    private Boolean isAutoExtension;
}
