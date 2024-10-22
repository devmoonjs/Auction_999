package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionItemController {

    private final AuctionItemService auctionItemService;

    /*
    @Todo n+1 -> fetch join
     */

    /**
     * 경매 생성
     * @param authUser
     * @param requestDto
     * @return AuctionCreateResponseDto
     */
    @PostMapping
    public ApiResponse<AuctionCreateResponseDto> createAuction(@AuthenticationPrincipal AuthUser authUser,
                                                                   @RequestBody AuctionCreateRequestDto requestDto) {
        return ApiResponse.created(auctionItemService.createAuction(authUser, requestDto));
    }

    /**
     * 경매 단건 조회
     * @param auctionId
     * @return AuctionResponseDto
     */
    @GetMapping("/{auctionId}")
    public ApiResponse<AuctionResponseDto> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.ok(auctionItemService.getAuction(auctionId));
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
        return ApiResponse.ok(auctionItemService.getAuctionList(pageable));
    }

    /**
     * 경매 물품 수정
     * @param authUser
     * @param auctionId
     * @param requestDto
     * @return AuctionResponseDto
     */
    @PutMapping("/{auctionId}")
    public ApiResponse<AuctionResponseDto> updateAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                                 @PathVariable Long auctionId,
                                                                 @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionItemService.updateAuctionItem(authUser, auctionId, requestDto));
    }

    /**
     * 경매 삭제
     * @param authUser
     * @param auctionId
     * @return 삭제 메시지
     */
    @DeleteMapping("/{auctionId}")
    public ApiResponse<String> deleteAuctionItem(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long auctionId) {
        return ApiResponse.ok(auctionItemService.deleteAuctionItem(authUser, auctionId));
    }

    // @Todo V2 : 검색
//    @GetMapping("/search")
//    public ApiResponse<Page<AuctionItemResponseDto>> searchAuctionItems(@RequestParam(defaultValue = "1") int page,
//                                                                        @RequestParam(defaultValue = "5") int size,
//                                                                        @RequestParam(required = false) String name,
//                                                                        @RequestParam(required = false) String category,
//                                                                        @RequestParam(required = false) String sortBy) {
//        return ApiResponse.ok(auctionItemService.searchAuctionItems(page, size, name, category, sortBy));
//    }
}
