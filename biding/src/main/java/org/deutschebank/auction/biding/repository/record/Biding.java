package org.deutschebank.auction.biding.repository.record;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "biding_list", indexes = {
        @Index(name = "productDetailIndex", columnList = "product_detail_id, bid_price DESC, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Biding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(nullable = false, name = "bid_price")
    private Double bidPrice;

    @Column(nullable = false, updatable = false)
    private String bider;

    @ManyToOne
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;

}
