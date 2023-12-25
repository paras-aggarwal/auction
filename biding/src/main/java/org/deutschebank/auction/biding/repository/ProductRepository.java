package org.deutschebank.auction.biding.repository;

import org.deutschebank.auction.biding.repository.record.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetail, Long> {

    List<ProductDetail> findByActive(boolean active);

}
