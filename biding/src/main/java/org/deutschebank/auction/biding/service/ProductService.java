package org.deutschebank.auction.biding.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.client.UserClient;
import org.deutschebank.auction.biding.client.model.response.ValidateUserResponse;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.deutschebank.auction.biding.model.Product;
import org.deutschebank.auction.biding.model.ProductStatus;
import org.deutschebank.auction.biding.model.ProductStatusResponse;
import org.deutschebank.auction.biding.model.Products;
import org.deutschebank.auction.biding.model.request.ToggleProductStatusRequest;
import org.deutschebank.auction.biding.repository.ProductRepository;
import org.deutschebank.auction.biding.repository.record.ProductDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
@EnableTransactionManagement
public class ProductService {

    private final UserClient userClient;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Products getProducts(boolean allowInactive) {
        List<ProductDetail> productRecords;
        if (allowInactive) {
            productRecords = productRepository.findAll();
        } else {
            productRecords = productRepository.findByActive(true);
        }
        return mapListToModel(productRecords);
    }

    @Transactional
    public Product addProduct(final String userToken, final Product product) {
        validateUser(userToken);
        ProductDetail productDetail = mapToRecord(product, userToken);
        ProductDetail savedProduct = saveProduct(productDetail);
        return mapToModel(savedProduct);
    }

    @Transactional
    public ProductStatusResponse toggleProductStatus(final String userToken, final Long productIdentifier,
                                                     final ToggleProductStatusRequest request) {
        try {
            ProductDetail productRecord = getProductById(productIdentifier);
            validateAuthor(userToken, productRecord.getAuthor());
            productRecord.setActive(request.isActive());
            saveProduct(productRecord);
            return ProductStatusResponse.builder()
                    .status(productRecord.isActive() ? ProductStatus.ACTIVE : ProductStatus.INACTIVE)
                    .build();
        } catch (final EntityNotFoundException e) {
            log.error("Invalid product id: {} provided", productIdentifier, e);
            throw new InvalidRequestException("", e.getCause(), "Invalid product indentifier provided");
        } catch (final Exception e) {
            log.error("Unexpected error occurred while activating product", e);
            throw e;
        }
    }

    private ProductDetail saveProduct(ProductDetail productDetail) {
        return productRepository.save(productDetail);
    }

    private ProductDetail getProductById(final Long productIdentifier) {
        return productRepository.getReferenceById(productIdentifier);
    }

    private Products mapListToModel(List<ProductDetail> productRecords) {
        List<Product> products = productRecords.stream()
                .map(this::mapToModel)
                .toList();
        return Products.builder()
                .products(products).build();
    }

    private Product mapToModel(ProductDetail savedProduct) {
        return Product.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .active(savedProduct.isActive())
                .startPrice(savedProduct.getStartPrice())
                .sold(savedProduct.isSold())
                .author(savedProduct.getAuthor())
                .build();
    }

    private ProductDetail mapToRecord(Product product, String userToken) {
        if (product != null && userToken != null) {
            ProductDetail productDetail = new ProductDetail();
            productDetail.setName(product.getName());
            productDetail.setDescription(product.getDescription());
            productDetail.setSold(false);
            productDetail.setActive(product.isActive());
            productDetail.setAuthor(userToken);
            productDetail.setStartPrice(product.getStartPrice());
            return productDetail;
        } else {
            throw new InvalidRequestException("", "empty product details provided");
        }
    }

    private void validateAuthor(String userToken, String productAuthor) {
        ValidateUserResponse user = userClient.validateAuthor(userToken, productAuthor);
        if (!user.isValid()) {
            throw new InvalidRequestException("", "Only product author can make these changes");
        }
    }

    private void validateUser(String userToken) {
        ValidateUserResponse user = userClient.validateUser(userToken);
        if (!user.isValid()) {
            throw new InvalidRequestException("", "User does not exist");
        }
    }

}
