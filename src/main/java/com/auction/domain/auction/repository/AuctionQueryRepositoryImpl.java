package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.response.AuctionResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.enums.ItemCategory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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
        List<Auction> auctionList = queryFactory
                .selectFrom(auction)
                .leftJoin(auction.item, item).fetchJoin()
                .where(nameEq(name), categoryEq(category))
                .orderBy(getSortOrder(sortBy))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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

    private BooleanExpression nameEq(String name) {
        return name != null ? auction.item.name.contains(name) : null;
    }

    private BooleanExpression categoryEq(String category) {
        return category != null ? auction.item.category.eq(ItemCategory.of(category)) : null;
    }

    private OrderSpecifier<?> getSortOrder(String sortBy) {
        if("priceLow".equalsIgnoreCase(sortBy)) {
            return auction.minPrice.asc();
        } else if("priceHigh".equalsIgnoreCase(sortBy)) {
            return auction.minPrice.desc();
        } else if("oldest".equalsIgnoreCase(sortBy)) {
            return auction.item.modifiedAt.asc();
        } else {
            return auction.item.modifiedAt.desc();
        }
    }
}
