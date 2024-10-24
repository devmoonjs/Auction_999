package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.AuctionHistoryDto;
import com.auction.domain.auction.dto.QAuctionHistoryDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.auction.domain.auction.entity.QAuctionHistory.auctionHistory;
import static com.auction.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class AuctionHistoryQueryRepositoryImpl implements AuctionHistoryQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<AuctionHistoryDto> findAuctionHistoryByAuctionId(long auctionId, long userId) {
        return jpaQueryFactory.select(
                        new QAuctionHistoryDto(auctionHistory.user.id, auctionHistory.price.max())
                )
                .from(auctionHistory)
                .where(
                        auctionHistory.auction.id.eq(auctionId),
                        auctionHistory.user.id.ne(userId)
                )
                .groupBy(user.id)
                .fetch();
    }
}
