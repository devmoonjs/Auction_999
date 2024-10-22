package com.auction.domain.auction.controller;

import com.auction.domain.auction.service.AuctionItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auction-items")
public class AuctionItemController {

    private final AuctionItemService auctionItemService;

    @PostMapping
    public
}
