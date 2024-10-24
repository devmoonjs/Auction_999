package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuctionQueryRepositoryImpl implements AuctionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AuctionQueryRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AuctionResponseDto> findByCustomSearch(Pageable pageable, String name, String category, String sortBy) {
//        List<AuctionItemResponseDto> content = queryFactory
//                .select(new QAuctionItemResponseDto(auctionItem.id, auctionItem.user.id, auctionItem.name, auctionItem.minPrice,
//                        auctionItem.maxPrice, auctionItem.category, auctionItem.expireAt, auctionItem.isAutoExtension,
//                        auctionItem.createdAt, auctionItem.modifiedAt))
//                .from(auctionItem)
//                .leftJoin(auctionItem.user, user)

        return null;
    }
}
