package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.auction.domain.auction.entity.QAuction.auction;
import static com.auction.domain.auction.entity.QItem.item;

public class AuctionQueryRepositoryImpl implements AuctionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AuctionQueryRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AuctionResponseDto> findAllCustom(Pageable pageable) {
        List<Auction> auctionList = queryFactory
                .selectFrom(auction)
                .leftJoin(auction.item, item).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(auction.count())
                .from(auction)
                .fetchOne()).orElse(0L);

        List<AuctionResponseDto> auctionResponseDtos = auctionList.stream()
                .map(AuctionResponseDto::from)
                .toList();

        return new PageImpl<>(auctionResponseDtos, pageable, total);
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
