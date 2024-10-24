package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionQueryRepository {
    Page<AuctionResponseDto> findByCustomSearch(Pageable pageable, String name, String category, String sortBy);
}
