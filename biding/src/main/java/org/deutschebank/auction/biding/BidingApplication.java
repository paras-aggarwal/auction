package org.deutschebank.auction.biding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class BidingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidingApplication.class, args);
	}

}
