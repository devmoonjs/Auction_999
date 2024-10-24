package com.auction.domain.point.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConvertRequestDto {
    @Min(value = 1000, message = "1000 포인트 이상부터 전환 가능합니다.")
    private int amount;
    private String bankCode;
    private String bankAccount;
}
