package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionItemResponseDto;
import com.auction.domain.auction.dto.response.QAuctionItemResponseDto;
import com.auction.domain.auction.entity.QAuctionItem;
import com.auction.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.auction.domain.auction.entity.QAuctionItem.auctionItem;
import static com.auction.domain.user.entity.QUser.user;

public class AuctionItemQueryRepositoryImpl implements AuctionItemQueryRepository{

    private final JPAQueryFactory queryFactory;

    public AuctionItemQueryRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AuctionItemResponseDto> findByCustomSearch(Pageable pageable, String name, String category, String sortBy) {
//        List<AuctionItemResponseDto> content = queryFactory
//                .select(new QAuctionItemResponseDto(auctionItem.id, auctionItem.user.id, auctionItem.name, auctionItem.minPrice,
//                        auctionItem.maxPrice, auctionItem.category, auctionItem.expireAt, auctionItem.isAutoExtension,
//                        auctionItem.createdAt, auctionItem.modifiedAt))
//                .from(auctionItem)
//                .leftJoin(auctionItem.user, user)

        return null;
    }
}
