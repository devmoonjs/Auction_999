package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionItemService {

    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;

    private Auction getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    private Auction getAuctionWithUser(AuthUser authUser, Long auctionId) {
        return auctionRepository.findByIdAndSellerId(auctionId, authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Transactional
    public AuctionCreateResponseDto createAuction(AuthUser authUser, AuctionCreateRequestDto requestDto) {
        Item item = Item.of(requestDto.getItem().getName(),
                requestDto.getItem().getDescription(),
                ItemCategory.of(requestDto.getItem().getCategory()));
        Item savedItem = itemRepository.save(item);
        Auction auction = Auction.of(savedItem, User.fromAuthUser(authUser), requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAt());
        Auction savedAuction = auctionRepository.save(auction);
        return AuctionCreateResponseDto.from(savedAuction);
    }

    public AuctionResponseDto getAuction(Long auctionId) {
        Auction auctionItem = getAuctionById(auctionId);
        return AuctionResponseDto.from(auctionItem);
    }

    public Page<AuctionResponseDto> getAuctionList(Pageable pageable) {
        return auctionRepository.findAllCustom(pageable);
    }

    @Transactional
    public AuctionResponseDto updateAuctionItem(AuthUser authUser, Long auctionId, AuctionItemChangeRequestDto requestDto) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        Item item = auction.getItem();

        if(requestDto.getName() != null) {
            item.changeName(requestDto.getName());
        }
        if(requestDto.getDescription() != null) {
            item.changeDescription(requestDto.getDescription());
        }
        if(requestDto.getCategory() != null) {
            item.changeCategory(ItemCategory.of(requestDto.getCategory()));
        }

        Item savedItem = itemRepository.save(item);
        auction.changeItem(savedItem);
        return AuctionResponseDto.from(auction);
    }

    @Transactional
    public String deleteAuctionItem(AuthUser authUser, Long auctionId) {
        Auction auction = getAuctionWithUser(authUser, auctionId);
        auctionRepository.delete(auction);
        return "물품이 삭제되었습니다.";
    }

    // @Todo
//    public Page<AuctionItemResponseDto> searchAuctionItems(int page, int size, String name, String category, String sortBy) {
//        Pageable pageable = PageRequest.of(page - 1, size);
//        return null;
//    }
}
