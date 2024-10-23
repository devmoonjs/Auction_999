package com.auction.domain.auction.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.entity.AuthUser;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.request.AuctionItemChangeRequestDto;
import com.auction.domain.auction.dto.request.AuctionCreateRequestDto;
import com.auction.domain.auction.dto.response.AuctionCreateResponseDto;
import com.auction.domain.auction.dto.response.AuctionItemResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.AuctionItem;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.repository.AuctionItemRepository;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionItemService {

    private final AuctionItemRepository auctionItemRepository;
    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;

    private AuctionItem getItem(Long auctionItemId) {
        return auctionItemRepository.findById(auctionItemId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    private AuctionItem getAuctionItemWithUser(AuthUser authUser, Long auctionItemId) {
        return auctionItemRepository.findByIdAndUserId(auctionItemId, authUser.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Transactional
    public AuctionCreateResponseDto createAuctionItem(AuthUser authUser, AuctionCreateRequestDto requestDto) {
//        AuctionItem auctionItem = AuctionItem.of(User.fromAuthUser(authUser), requestDto.getName(), requestDto.getContent(),
//                requestDto.getMinPrice(), requestDto.getMinPrice(), ItemCategory.of(requestDto.getCategory()), requestDto.getExpireAt(), requestDto.isAutoExtension());
//        AuctionItem savedAuctionItem = auctionItemRepository.save(auctionItem);
//        return AuctionItemResponseDto.from(savedAuctionItem);

        Item item = Item.of(requestDto.getItem().getName(),
                requestDto.getItem().getDescription(),
                ItemCategory.of(requestDto.getItem().getCategory()));
        Item savedItem = itemRepository.save(item);
        Auction auction = Auction.of(savedItem, User.fromAuthUser(authUser), requestDto.getMinPrice(), requestDto.isAutoExtension(), requestDto.getExpireAt());
        Auction savedAuction = auctionRepository.save(auction);
        return null;
    }

    public AuctionItemResponseDto getAuctionItem(Long auctionItemId) {
        AuctionItem auctionItem = getItem(auctionItemId);
        return AuctionItemResponseDto.from(auctionItem);
    }

    public Page<AuctionItemResponseDto> getAuctionItemList(Pageable pageable) {
        return auctionItemRepository.findAll(pageable).map(AuctionItemResponseDto::from);
    }

    @Transactional
    public AuctionItemResponseDto updateAuctionItem(AuthUser authUser, Long auctionItemId, AuctionItemChangeRequestDto requestDto) {
        AuctionItem auctionItem = getAuctionItemWithUser(authUser, auctionItemId);
        if(requestDto.getName() != null) {
            auctionItem.changeName(requestDto.getName());
        }
        if(requestDto.getContent() != null) {
            auctionItem.changeContent(requestDto.getContent());
        }
        if(requestDto.getMinPrice() != null) {
            auctionItem.changeMinPrice(requestDto.getMinPrice());
        }
        if(requestDto.getCategory() != null) {
            auctionItem.changeCategory(ItemCategory.of(requestDto.getCategory()));
        }
        if(requestDto.getIsAutoExtension() != null) {
            auctionItem.changeAutoExtension(requestDto.getIsAutoExtension());
        }
        AuctionItem savedAuctionItem = auctionItemRepository.save(auctionItem);
        return AuctionItemResponseDto.from(savedAuctionItem);
    }

    @Transactional
    public String deleteAuctionItem(AuthUser authUser, Long auctionItemId) {
        AuctionItem auctionItem = getAuctionItemWithUser(authUser, auctionItemId);
        auctionItemRepository.delete(auctionItem);
        return "물품이 삭제되었습니다.";
    }

    public Page<AuctionItemResponseDto> searchAuctionItems(int page, int size, String name, String category, String sortBy) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return null;
    }
}
