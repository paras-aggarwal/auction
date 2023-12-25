package org.deutschebank.auction.biding.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Products {

    List<Product> products;

}
