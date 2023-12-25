package org.deutschebank.auction.biding.controller;

import lombok.RequiredArgsConstructor;
import org.deutschebank.auction.biding.model.BidStatusResponse;
import org.deutschebank.auction.biding.model.BidWinner;
import org.deutschebank.auction.biding.model.request.PlaceBidRequest;
import org.deutschebank.auction.biding.service.BidingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidingService bidingService;

    @PostMapping("/product/{productIdentifier}/bid")
    public BidStatusResponse placeBid(@RequestHeader("X-User-Token") String userToken,
                                      @PathVariable("productIdentifier") Long productIdentifier,
                                      @RequestBody PlaceBidRequest request) {
        return bidingService.placeBid(userToken, productIdentifier, request);
    }

    @PostMapping("/product/{productIdentifier}/sold")
    public BidWinner soldProduct(@RequestHeader("X-User-Token") String userToken,
                                 @PathVariable("productIdentifier") Long productIdentifier) {
        return bidingService.soldProduct(userToken, productIdentifier);
    }
}
