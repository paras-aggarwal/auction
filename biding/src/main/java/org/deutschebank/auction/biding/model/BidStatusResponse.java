package org.deutschebank.auction.biding.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BidStatusResponse {

    BidStatus status;
    String message;

}
