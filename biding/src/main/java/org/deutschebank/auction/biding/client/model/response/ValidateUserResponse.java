package org.deutschebank.auction.biding.client.model.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidateUserResponse {

    boolean valid;

}
