package org.deutschebank.auction.biding.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Product {

    Long id;
    String name;
    String description;
    Double startPrice;
    boolean sold;
    boolean active;
    String author;

}
