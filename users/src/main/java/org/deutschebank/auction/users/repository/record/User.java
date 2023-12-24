package org.deutschebank.auction.users.repository.record;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "user_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @GeneratedValue
    @UuidGenerator
    private String token;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    private String city;

    private Long pincode;

    @Column(name = "address_additional_info")
    private String additionalAddressInfo;

    private String country;

}
