package org.deutschebank.auction.biding.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.deutschebank.auction.biding.model.AuctionWinner;
import org.deutschebank.auction.biding.model.BidStatus;
import org.deutschebank.auction.biding.model.BidStatusResponse;
import org.deutschebank.auction.biding.model.BidWinner;
import org.deutschebank.auction.biding.model.request.PlaceBidRequest;
import org.deutschebank.auction.biding.repository.BidingRepository;
import org.deutschebank.auction.biding.repository.ProductRepository;
import org.deutschebank.auction.biding.repository.record.Biding;
import org.deutschebank.auction.biding.repository.record.ProductDetail;
import org.deutschebank.auction.biding.service.common.UserValidatorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidingService {

    private final UserValidatorService userValidatorService;

    private final ProductRepository productRepository;
    private final BidingRepository bidingRepository;

    @Transactional
    public BidStatusResponse placeBid(final String userToken, final Long productIdentifier,
                                      final PlaceBidRequest request) {
        try {
            final ProductDetail productRecord = productRepository.findByIdAndActive(productIdentifier, true);
            if (productRecord.getAuthor().equals(userToken)) {
                log.warn("User: {} is the author of this product: {}", userToken, productIdentifier);
                throw new InvalidRequestException("Author is not allowed to place the bid");
            }
            userValidatorService.validateUser(userToken);
            return processBid(request, userToken, productRecord);
        } catch (final EntityNotFoundException e) {
            log.warn("product: {} not found", productIdentifier, e);
            throw new InvalidRequestException("", e.getCause(), "Product not found for biding");
        } catch (final Exception e) {
            log.error("Error occurred while placing bid for product: {}", productIdentifier, e);
            throw e;
        }
    }

    @Transactional
    public BidWinner soldProduct(final String userToken, final Long productIdentifier) {
        final ProductDetail productRecord = productRepository.getReferenceById(productIdentifier);
        if (!productRecord.getAuthor().equals(userToken)) {
            log.warn("Only product author can end auction for a product");
            throw new InvalidRequestException("Auction can only be ended by product author");
        }
        final Biding highestBid = bidingRepository.findFirstByProductDetailOrderByBidPriceDescTimestampAsc(productRecord);
        productRecord.setSold(true);
        productRecord.setActive(false);
        productRecord.setSoldPrice(highestBid != null ? highestBid.getBidPrice() : null);
        productRepository.save(productRecord);

        final AuctionWinner.AuctionWinnerBuilder auctionWinnerBuilder = AuctionWinner.builder();
        if (highestBid != null) {
            auctionWinnerBuilder
                    .winner(highestBid.getBider())
                    .winningBid(highestBid.getBidPrice());
        }
        return BidWinner.builder()
                .productIdentifier(productIdentifier)
                .winnerDetails(auctionWinnerBuilder.build())
                .build();
    }

    private BidStatusResponse processBid(final PlaceBidRequest request, final String userToken,
                                         final ProductDetail productRecord) {
        if (request.getPrice() <= 0) {
            return BidStatusResponse.builder()
                    .status(BidStatus.REJECTED)
                    .message("Bid amount should be greater than 0")
                    .build();
        }
        if (productRecord.getStartPrice() > request.getPrice()) {
            return BidStatusResponse.builder()
                    .status(BidStatus.REJECTED)
                    .message("Bid should be greater than or equal to minimum bid price")
                    .build();
        }
        return saveBid(request, userToken, productRecord);
    }

    private BidStatusResponse saveBid(final PlaceBidRequest request, final String userToken,
                                      final ProductDetail productRecord) {
        final Biding bidingRecord = new Biding();
        bidingRecord.setTimestamp(new Date());
        bidingRecord.setBidPrice(request.getPrice());
        bidingRecord.setBider(userToken);
        bidingRecord.setProductDetail(productRecord);
        final Biding recordedBid = bidingRepository.save(bidingRecord);

        if (recordedBid.getId() != null) {
            return BidStatusResponse.builder()
                    .status(BidStatus.PLACED)
                    .build();
        } else {
            return BidStatusResponse.builder()
                    .status(BidStatus.REJECTED)
                    .message("UNKNOWN")
                    .build();
        }
    }

}
