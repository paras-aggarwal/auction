package org.deutschebank.auction.biding.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.client.model.UserResponse;
import org.deutschebank.auction.biding.client.model.response.ValidateUserResponse;
import org.deutschebank.auction.biding.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceRestUrl;

    public ValidateUserResponse validateUser(final String userToken) {
        final UserResponse user = getUser(userToken);
        if (user == null) {
            log.error("User: {} is not valid", userToken);
            return ValidateUserResponse.builder()
                    .valid(false).build();
        } else {
            return ValidateUserResponse.builder()
                    .valid(true).build();
        }
    }

    public ValidateUserResponse validateAuthor(final String userToken, String productAuthor) {
        final UserResponse user = getUser(userToken);
        if (user == null) {
            return ValidateUserResponse.builder()
                    .valid(false).build();
        } else if (productAuthor.equals(user.getUserToken())) {
            return ValidateUserResponse.builder()
                    .valid(true).build();
        } else {
            return ValidateUserResponse.builder()
                    .valid(false).build();
        }
    }

    private UserResponse getUser(final String userToken) {
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(userServiceRestUrl)
                    .pathSegment("user", userToken)
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<Object>(headers);
            ResponseEntity<UserResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, UserResponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND || responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return null;
            } else {
                throw new BusinessException(responseEntity.getStatusCode().toString(), "Error while getting user details");
            }
        } catch (Exception e) {
            log.error("Error occurred while getting user details: {}", userToken, e);
            throw new BusinessException("", e.getCause());
        }
    }

}
