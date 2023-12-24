package org.deutschebank.auction.users.repository;

import org.deutschebank.auction.users.repository.record.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    User findByPhoneNumberAndEmail(String phoneNumber, String email);

    User findByToken(String token);

}
