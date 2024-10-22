package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemCreateRequestDto;
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

    /**
     * 옥션 물품 저장
     * @param authUser
     * @param requestDto
     * @return AuctionItemResponseDto
     */
    @PostMapping
    public ApiResponse<AuctionItemResponseDto> createAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                                 @RequestBody AuctionItemCreateRequestDto requestDto) {
        return ApiResponse.created(auctionItemService.createAuctionItem(authUser, requestDto));
    }

    /**
     * 옥션 물품 단건 조회
     * @param auctionItemId
     * @return AuctionItemResponseDto
     */
    @GetMapping("/{auctionItemId}")
    public ApiResponse<AuctionItemResponseDto> getAuctionItem(@PathVariable Long auctionItemId) {
        return ApiResponse.ok(auctionItemService.getAuctionItem(auctionItemId));
    }

    /**
     * 옥션 물품 전체 조회
     * @return Page<AuctionItemResponseDto>
     */
    // @Todo N+1 문제 해결하기 (user.getId() 로 user 수만큼 쿼리문 나감)
    @GetMapping
    public ApiResponse<Page<AuctionItemResponseDto>> getAuctionItemList(@PageableDefault(size = 5, sort = "modifiedAt", direction = Sort.Direction.DESC)
                                                                            Pageable pageable)
    {
        return ApiResponse.ok(auctionItemService.getAuctionItemList(pageable));
    }

    /**
     * 옥션 물품 정보 수정
     * @param authUser
     * @param auctionItemId
     * @param requestDto
     * @return AuctionItemResponseDto
     */
    @PutMapping("/{auctionItemId}")
    public ApiResponse<AuctionItemResponseDto> updateAuctionItem(@AuthenticationPrincipal AuthUser authUser,
                                                                 @PathVariable Long auctionItemId,
                                                                 @RequestBody AuctionItemChangeRequestDto requestDto) {
        return ApiResponse.ok(auctionItemService.updateAuctionItem(authUser, auctionItemId, requestDto));
    }

    /**
     * 옥션 물품 삭제
     * @param authUser
     * @param auctionItemId
     * @return 삭제 메시지
     */
    @DeleteMapping("/{auctionItemId}")
    public ApiResponse<String> deleteAuctionItem(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long auctionItemId) {
        return ApiResponse.ok(auctionItemService.deleteAuctionItem(authUser, auctionItemId));
    }
}
