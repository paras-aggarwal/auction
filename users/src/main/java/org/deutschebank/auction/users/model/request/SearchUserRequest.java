package org.deutschebank.auction.users.model.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchUserRequest {

    String phoneNumber;
    String email;

}
