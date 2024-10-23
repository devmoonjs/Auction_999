package com.auction.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AuctionCreateRequestDto {
    @Min(value = 1000, message = "최소 금액은 1000원 입니다.")
    private int minPrice;
    private boolean isAutoExtension;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireAt;
    private ItemCreateRequestDto item;
}
