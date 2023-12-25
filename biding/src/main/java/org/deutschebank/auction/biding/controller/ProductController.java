package org.deutschebank.auction.biding.controller;

import lombok.RequiredArgsConstructor;
import org.deutschebank.auction.biding.model.Product;
import org.deutschebank.auction.biding.model.ProductStatusResponse;
import org.deutschebank.auction.biding.model.Products;
import org.deutschebank.auction.biding.model.request.ToggleProductStatusRequest;
import org.deutschebank.auction.biding.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public Products getProducts(@RequestParam(value = "allow_inactive", defaultValue = "true") boolean allowInactive) {
        return productService.getProducts(allowInactive);
    }

    @PostMapping("/product")
    public Product addProduct(@RequestHeader("X-User-Token") String userToken,
                              @RequestBody Product product) {
        return productService.addProduct(userToken, product);
    }

    @PatchMapping("/product/{productIdentifier}")
    public ProductStatusResponse toggleProductStatus(@RequestHeader("X-User-Token") String userToken,
                                                     @PathVariable("productIdentifier") Long productIdentifier,
                                                     @RequestBody ToggleProductStatusRequest request) {
        return productService.toggleProductStatus(userToken, productIdentifier, request);
    }

}
