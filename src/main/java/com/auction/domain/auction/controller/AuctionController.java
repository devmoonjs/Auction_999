package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.BidCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.dto.response.BidCreateResponseDto;
import com.auction.domain.auction.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    /**
     * 경매 생성
     * @param authUser
     * @param requestDto
     * @return AuctionCreateResponseDto
     */
    @PostMapping("/v1/auctions")
    public ApiResponse<AuctionCreateResponseDto> createAuction(@AuthenticationPrincipal AuthUser authUser,
                                                               @RequestBody AuctionCreateRequestDto requestDto) {
        return ApiResponse.created(auctionService.createAuction(authUser, requestDto));
    }

    /**
     * 경매 단건 조회
     * @param auctionId
     * @return AuctionResponseDto
     */
    @GetMapping("/v1/auctions/{auctionId}")
    public ApiResponse<AuctionResponseDto> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.ok(auctionService.getAuction(auctionId));
    }

    /**
     * 경매 전체 조회
     * @param pageable
     * @return AuctionResponseDto
     */
    @GetMapping
    public ApiResponse<Page<AuctionResponseDto>> getAuctionList(@PageableDefault(size = 5, sort = "modifiedAt", direction = Sort.Direction.DESC)
                                                                Pageable pageable)
    {
        return ApiResponse.ok(auctionService.getAuctionList(pageable));
    }

    /**
     * 경매 물품 수정
     * @param authUser
     * @param auctionId
     * @param requestDto
     * @return AuctionResponseDto
     */
    @PutMapping("/v1/auctions/{auctionId}")
    public ApiResponse<AuctionResponseDto> updateAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                             @PathVariable Long auctionId,
                                                             @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionService.updateAuctionItem(authUser, auctionId, requestDto));
    }

    /**
     * 경매 삭제
     * @param authUser
     * @param auctionId
     * @return 삭제 메시지
     */
    @DeleteMapping("/v1/auctions/{auctionId}")
    public ApiResponse<String> deleteAuctionItem(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long auctionId) {
        return ApiResponse.ok(auctionService.deleteAuctionItem(authUser, auctionId));
    }

    /**
     * 조건 검색
     * @param pageable 페이지 조건으로 검색 : ?page=&size=
     * @param name 경매 물품 이름으로 검색 (부분 검색 허용) : ?name=
     * @param category 경매 물품 카테고리로 검색 : ?category=
     * @param sortBy 조건별 정렬 검색 (priceLow:최소가격 낮은 순, priceHigh:최소가격 높은 순, oldest:오래된 순, default:최신 순) : ?sortBy=
     * @return Page<AuctionResponseDto>
     */
    @GetMapping("/v1/auctions/search")
    public ApiResponse<Page<AuctionResponseDto>> searchAuctionItems(@PageableDefault(size = 5) Pageable pageable,
                                                                    @RequestParam(required = false) String name,
                                                                    @RequestParam(required = false) String category,
                                                                    @RequestParam(required = false) String sortBy) {
        return ApiResponse.ok(auctionService.searchAuctionItems(pageable, name, category, sortBy));
    }

    /**
     * 입찰 등록
     * @param authUser
     * @param auctionId             경매 식별자
     * @param bidCreateRequestDto
     * @return
     */
    @PostMapping("/v1/auctions/{auctionId}/bid")
    public ApiResponse<BidCreateResponseDto> createBid(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("auctionId") Long auctionId,
            @Valid @RequestBody BidCreateRequestDto bidCreateRequestDto
    ) {
        return ApiResponse.ok(auctionService.createBid(authUser, auctionId, bidCreateRequestDto));
    }
}
