package com.auction.domain.point.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConvertRequestDto {
    private int amount;
    private String bankCode;
    private String bankAccount;
}
