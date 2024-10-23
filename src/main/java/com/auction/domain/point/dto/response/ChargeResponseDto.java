package com.auction.domain.point.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargeResponseDto {
    private int chargedAmount;
    private int balanceAmount;
}
