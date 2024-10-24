package com.auction.domain.auction.controller;

import com.auction.common.apipayload.ApiResponse;
import com.auction.common.entity.AuthUser;
import com.auction.domain.auction.dto.response.AuctionItemImageResponseDto;
import com.auction.domain.auction.service.AuctionItemImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions/{auctionId}/items/images")
public class AuctionItemImageController {

    private final AuctionItemImageService auctionItemImageService;
    
    /*
    @Todo 사진 조회 + auction 등록할 때 같이?
     */

    /**
     * 경매 물품 사진 등록
     * @param authUser
     * @param auctionId
     * @param files
     * @return List<AuctionItemImageResponseDto>
     * @throws IOException
     */
    @PostMapping
    public ApiResponse<List<AuctionItemImageResponseDto>> uploadImages(@AuthenticationPrincipal AuthUser authUser,
                                                                       @PathVariable("auctionId") Long auctionId,
                                                                       @RequestParam("files") List<MultipartFile> files) throws IOException {
        return ApiResponse.created(auctionItemImageService.uploadImages(authUser, auctionId, files));
    }

    /**
     * 경매 물품 삭제
     * @param authUser
     * @param auctionId
     * @param imageId
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/{imageId}")
    public ApiResponse<String> deleteImage(@AuthenticationPrincipal AuthUser authUser,
                                           @PathVariable("auctionId") Long auctionId,
                                           @PathVariable("imageId") Long imageId) {
        return ApiResponse.ok(auctionItemImageService.deleteImages(authUser, auctionId, imageId));
    }
}
