package org.deutschebank.auction.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class User {

    String userToken;
    String firstName;
    String lastName;
    String phoneNumber;
    String email;
    String street;
    String houseNumber;
    String city;
    Long pincode;
    String additionalAddressInfo;
    String country;

}
