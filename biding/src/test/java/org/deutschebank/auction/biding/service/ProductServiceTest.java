package org.deutschebank.auction.biding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.deutschebank.auction.biding.client.UserClient;
import org.deutschebank.auction.biding.client.model.UserResponse;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.deutschebank.auction.biding.model.Product;
import org.deutschebank.auction.biding.model.ProductStatus;
import org.deutschebank.auction.biding.model.ProductStatusResponse;
import org.deutschebank.auction.biding.model.Products;
import org.deutschebank.auction.biding.model.request.ToggleProductStatusRequest;
import org.deutschebank.auction.biding.repository.ProductRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void givenValidUserAndProduct_whenAddProduct_thenReturnSavedProduct() throws URISyntaxException, JsonProcessingException {
        UserResponse dummyUser = getDummyUser();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );

        Product savedProduct = productService.addProduct(dummyUser.getUserToken(), testProduct);

        Assertions.assertThat(savedProduct.getId()).isNotNull();
        mockServer.verify();
    }

    @Test
    void givenInvalidUserAndProduct_whenAddProduct_thenReturnException() throws URISyntaxException, JsonProcessingException {
        String userToken = UUID.randomUUID().toString();
        Product testProduct = getTestProduct(userToken, true);
        UserResponse user = null;

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + userToken)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(user))
                );

        assertThrows(InvalidRequestException.class, () -> productService.addProduct(userToken, testProduct));
    }

    @Test
    void givenProductsInRecord_whenGetProducts_thenReturnAllProducts() throws JsonProcessingException, URISyntaxException {
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
        Products products = productService.getProducts(true);

        Assertions.assertThat(products.getProducts().size()).isEqualTo(1);
    }

    @Test
    void givenInactiveProductsInRecord_whenGetProductsNoInactive_thenReturnNoProducts() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), false);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );

        productService.addProduct(dummyUser.getUserToken(), testProduct);
        Products products = productService.getProducts(false);

        Assertions.assertThat(products.getProducts().size()).isEqualTo(0);
    }

    @Test
    void givenProductInRecord_whenToggleProductStatus_thenReturnProductStatus() throws JsonProcessingException, URISyntaxException {
        UserResponse dummyUser = getDummyUser();
        Product testProduct = getTestProduct(dummyUser.getUserToken(), true);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.twice(), requestTo(new URI("http://localhost:8081/user/" + dummyUser.getUserToken())))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(dummyUser))
                );

        Product product = productService.addProduct(dummyUser.getUserToken(), testProduct);
        ToggleProductStatusRequest request = ToggleProductStatusRequest.builder().active(false).build();
        ProductStatusResponse response = productService.toggleProductStatus(dummyUser.getUserToken(), product.getId(), request);

        Assertions.assertThat(response.getStatus()).isEqualTo(ProductStatus.INACTIVE);
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
