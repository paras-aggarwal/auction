package org.deutschebank.auction.biding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.deutschebank.auction.biding.client.model.UserResponse;
import org.deutschebank.auction.biding.model.BidStatus;
import org.deutschebank.auction.biding.model.BidStatusResponse;
import org.deutschebank.auction.biding.model.BidWinner;
import org.deutschebank.auction.biding.model.Product;
import org.deutschebank.auction.biding.model.request.PlaceBidRequest;
import org.deutschebank.auction.biding.repository.BidingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BidingServiceTest {

    @Autowired
    private BidingService bidingService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BidingRepository bidingRepository;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void givenValidProductAndUser_whenPlaceBid_thenReturnSuccess() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        String newToken = UUID.randomUUID().toString();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + newToken)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        productService.addProduct(dummyUser.getUserToken(), testProduct);
        BidStatusResponse response = bidingService.placeBid(newToken, testProduct.getId(), PlaceBidRequest.builder().price(2000.00).build());

        Assertions.assertThat(response.getStatus()).isEqualTo(BidStatus.PLACED);
    }

    @Test
    void givenValidProductAndUserInvalidAmount_whenPlaceBid_thenReturnRejected() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        String newToken = UUID.randomUUID().toString();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + newToken)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        productService.addProduct(dummyUser.getUserToken(), testProduct);
        BidStatusResponse response = bidingService.placeBid(newToken, testProduct.getId(), PlaceBidRequest.builder().price(1.00).build());

        Assertions.assertThat(response.getStatus()).isEqualTo(BidStatus.REJECTED);
    }

    @Test
    void givenValidBids_whenSoldProduct_thenReturnWinner() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        String newToken = UUID.randomUUID().toString();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + newToken)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        productService.addProduct(dummyUser.getUserToken(), testProduct);
        bidingService.placeBid(newToken, testProduct.getId(), PlaceBidRequest.builder().price(2000.00).build());

        BidWinner bidWinner = bidingService.soldProduct(dummyUser.getUserToken(), testProduct.getId());

        Assertions.assertThat(bidWinner.getProductIdentifier()).isNotNull();
        Assertions.assertThat(bidWinner.getWinnerDetails().getWinner()).isEqualTo(newToken);
    }

    @Test
    void givenNoBids_whenSoldProduct_thenReturnNoWinner() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );
        productService.addProduct(dummyUser.getUserToken(), testProduct);

        BidWinner bidWinner = bidingService.soldProduct(dummyUser.getUserToken(), testProduct.getId());

        Assertions.assertThat(bidWinner.getProductIdentifier()).isNotNull();
        Assertions.assertThat(bidWinner.getWinnerDetails().getWinner()).isNull();
    }

    private Product getTestProduct(String userToken, boolean active) {
        return Product.builder()
                .id(1L)
                .name("test product")
                .description("test description")
                .active(active)
                .author(userToken)
                .sold(false)
                .startPrice(1000.00)
                .build();
    }

    private UserResponse getDummyUser() {
        return UserResponse.builder()
                .userToken(UUID.randomUUID().toString())
                .firstName("test")
                .lastName("qa")
                .build();
    }

}
