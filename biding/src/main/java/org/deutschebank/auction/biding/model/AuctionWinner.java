package org.deutschebank.auction.biding.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuctionWinner {

    String winner;
    Double winningBid;

}
