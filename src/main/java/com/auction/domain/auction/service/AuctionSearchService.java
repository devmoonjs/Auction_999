package com.auction.domain.auction.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.auction.domain.auction.dto.response.ItemDocumentResponseDto;
import com.auction.domain.auction.dto.response.ItemSearchResponseDto;
import com.auction.domain.auction.elasticsearch.repository.ItemElasticRepository;
import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuctionSearchService {

    private final ItemRepository itemRepository;
    private final ItemElasticRepository elasticRepository;
    private final ElasticsearchClient elasticsearchClient;

    // ES 적용 X
    public Page<ItemSearchResponseDto> searchAuctionItemsByKeyword(Pageable pageable, String keyword) {
        return itemRepository.findByKeyword(pageable, keyword);
    }
    public Page<ItemSearchResponseDto> searchAllAuctionItems(Pageable pageable) {
        List<Item> all = itemRepository.findAll();
        List<ItemSearchResponseDto> dtos = all.stream().map(ItemSearchResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, all.size());
    }

    // ES 적용 O
    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByName(Pageable pageable, String keyword) throws IOException {
        Query query = MatchQuery.of(m -> m
                .field("name")
                .query(keyword)
                .fuzziness("AUTO")
        )._toQuery();

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("item_documents")
                .query(query)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );

        SearchResponse<ItemDocument> response = elasticsearchClient.search(searchRequest, ItemDocument.class);

        List<ItemDocumentResponseDto> dtos = response.hits().hits().stream()
                .map(hit -> ItemDocumentResponseDto.from(hit.source()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, response.hits().total().value());
    }

    public Page<ItemDocumentResponseDto> elasticSearchAllAuctionItems(Pageable pageable) {
        List<ItemDocument> documents = (List<ItemDocument>) elasticRepository.findAll();
        List<ItemDocumentResponseDto> dtos = documents.stream().map(ItemDocumentResponseDto::from).toList();
        return new PageImpl<>(dtos, pageable, documents.size());
    }

    public Page<ItemDocumentResponseDto> elasticSearchAuctionItemsByKeyword(Pageable pageable, String keyword) throws IOException {
        // BoolQueryBuilder를 사용하여 검색 조건을 생성
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        if (keyword != null && !keyword.isEmpty()) {
            boolQueryBuilder.should(MatchQuery.of(m -> m.field("name").query(keyword).boost(2.0F))._toQuery())
                    .should(MatchQuery.of(m -> m.field("description").query(keyword))._toQuery());
        }

        Query query = Query.of(q -> q.bool(boolQueryBuilder.build()));

        // 검색 요청 생성 및 실행 (정렬 없이 기본 정렬 사용)
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("item_documents")
                .query(query)
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );

        SearchResponse<ItemDocument> response = elasticsearchClient.search(searchRequest, ItemDocument.class);

        List<ItemDocumentResponseDto> dtos = response.hits().hits().stream()
                .map(hit -> ItemDocumentResponseDto.from(hit.source()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, response.hits().total().value());
    }

}
