package org.deutschebank.auction.biding.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.deutschebank.auction.biding.model.Product;
import org.deutschebank.auction.biding.model.ProductStatus;
import org.deutschebank.auction.biding.model.ProductStatusResponse;
import org.deutschebank.auction.biding.model.Products;
import org.deutschebank.auction.biding.model.request.ToggleProductStatusRequest;
import org.deutschebank.auction.biding.repository.ProductRepository;
import org.deutschebank.auction.biding.repository.record.ProductDetail;
import org.deutschebank.auction.biding.service.common.UserValidaterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    private final UserValidaterService userValidaterService;

    public Products getProducts(final boolean allowInactive) {
        final List<ProductDetail> productRecords;
        if (allowInactive) {
            productRecords = productRepository.findAll();
        } else {
            productRecords = productRepository.findByActive(true);
        }
        return mapListToModel(productRecords);
    }

    @Transactional
    public Product addProduct(final String userToken, final Product product) {
        userValidaterService.validateUser(userToken);
        if (product.getStartPrice() == null || product.getStartPrice() <= 0) {
            log.warn("Product start bid should be greater than 0");
            throw new InvalidRequestException("", "Product's minimum bid price should be greater than 0");
        }
        final ProductDetail productDetail = mapToRecord(product, userToken);
        final ProductDetail savedProduct = saveProduct(productDetail);
        return mapToModel(savedProduct);
    }

    @Transactional
    public ProductStatusResponse toggleProductStatus(final String userToken, final Long productIdentifier,
                                                     final ToggleProductStatusRequest request) {
        try {
            final ProductDetail productRecord = getProductById(productIdentifier);
            if (!userToken.equals(productRecord.getAuthor())) {
                log.warn("User: {} is not the author of the product: {}", userToken, productIdentifier);
                throw new InvalidRequestException("", "Only product author can make this change");
            }
            if (productRecord.isSold()) {
                log.warn("Product is already sold and status cannot be changed");
                throw new InvalidRequestException("", "Product status cannot be changed for already sold out product");
            }
            productRecord.setActive(request.isActive());
            saveProduct(productRecord);
            return ProductStatusResponse.builder()
                    .status(productRecord.isActive() ? ProductStatus.ACTIVE : ProductStatus.INACTIVE)
                    .build();
        } catch (final EntityNotFoundException e) {
            log.error("Invalid product id: {} provided", productIdentifier, e);
            throw new InvalidRequestException("", e.getCause(), "Invalid product indentifier provided");
        } catch (final InvalidRequestException e) {
            throw e;
        } catch (final Exception e) {
            log.error("Unexpected error occurred while activating product", e);
            throw e;
        }
    }

    private ProductDetail saveProduct(final ProductDetail productDetail) {
        return productRepository.save(productDetail);
    }

    private ProductDetail getProductById(final Long productIdentifier) {
        return productRepository.getReferenceById(productIdentifier);
    }

    private Products mapListToModel(final List<ProductDetail> productRecords) {
        final List<Product> products = productRecords.stream()
                .map(this::mapToModel)
                .toList();
        return Products.builder()
                .products(products).build();
    }

    private Product mapToModel(final ProductDetail savedProduct) {
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

    private ProductDetail mapToRecord(final Product product, final String userToken) {
        if (product != null && userToken != null) {
            final ProductDetail productDetail = new ProductDetail();
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

}
