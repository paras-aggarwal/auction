package org.deutschebank.auction.biding.model.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ToggleProductStatusRequest {

    boolean active;

}
