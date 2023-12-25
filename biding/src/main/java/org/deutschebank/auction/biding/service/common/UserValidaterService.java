package org.deutschebank.auction.biding.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.client.UserClient;
import org.deutschebank.auction.biding.client.model.UserResponse;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class UserValidaterService {

    private final UserClient userClient;

    public void validateUser(final String userToken) {
        final UserResponse user = userClient.getUser(userToken);
        if (user == null) {
            log.warn("User: {} is not valid", userToken);
            throw new InvalidRequestException("", "User not found");
        }
    }

}
