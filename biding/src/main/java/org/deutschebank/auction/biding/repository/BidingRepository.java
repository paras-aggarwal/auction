package org.deutschebank.auction.biding.repository;

import org.deutschebank.auction.biding.repository.record.Biding;
import org.deutschebank.auction.biding.repository.record.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidingRepository extends JpaRepository<Biding, Long> {

    Biding findByProductDetailOrderByBidPriceDescTimestampAsc(ProductDetail productDetail);
}
