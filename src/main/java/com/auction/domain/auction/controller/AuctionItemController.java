package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionItemResponseDto;
import com.auction.domain.auction.service.AuctionItemService;
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
    
    @PostMapping
    public ApiResponse<AuctionCreateResponseDto> createAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                                   @RequestBody AuctionCreateRequestDto requestDto) {
        return ApiResponse.created(auctionItemService.createAuctionItem(authUser, requestDto));
    }

    @GetMapping("/{auctionItemId}")
    public ApiResponse<AuctionItemResponseDto> getAuctionItem(@PathVariable Long auctionItemId) {
        return ApiResponse.ok(auctionItemService.getAuctionItem(auctionItemId));
    }

    @GetMapping
    public ApiResponse<Page<AuctionItemResponseDto>> getAuctionItemList(@PageableDefault(size = 5, sort = "modifiedAt", direction = Sort.Direction.DESC)
                                                                            Pageable pageable)
    {
        return ApiResponse.ok(auctionItemService.getAuctionItemList(pageable));
    }

    @PutMapping("/{auctionItemId}")
    public ApiResponse<AuctionItemResponseDto> updateAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                                 @PathVariable Long auctionItemId,
                                                                 @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionItemService.updateAuctionItem(authUser, auctionItemId, requestDto));
    }

    @DeleteMapping("/{auctionItemId}")
    public ApiResponse<String> deleteAuctionItem(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long auctionItemId) {
        return ApiResponse.ok(auctionItemService.deleteAuctionItem(authUser, auctionItemId));
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
