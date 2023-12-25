package org.deutschebank.auction.biding.controller;

import lombok.RequiredArgsConstructor;
import org.deutschebank.auction.biding.model.BidStatusResponse;
import org.deutschebank.auction.biding.model.request.PlaceBidRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BidController {

    @PostMapping("/product/{productIdentifier}/bid")
    public BidStatusResponse placeBid(@RequestHeader("X-User-Token") String userToken,
                                      @PathVariable("productIdentifier") String productIdentifier,
                                      @RequestBody PlaceBidRequest request) {
        return null;
    }

    @PostMapping("/product/{productIdentifier}/winner")
    public BidStatusResponse getWinner(@RequestHeader("X-User-Token") String userToken,
                                       @PathVariable("productIdentifier") String productIdentifier) {
        return null;
    }
}
