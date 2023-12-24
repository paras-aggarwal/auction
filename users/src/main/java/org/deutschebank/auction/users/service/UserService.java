package org.deutschebank.auction.users.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.users.model.User;
import org.deutschebank.auction.users.model.request.SearchUserRequest;
import org.deutschebank.auction.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@EnableTransactionManagement
@Log4j2
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(final String userToken) throws Exception {
        org.deutschebank.auction.users.repository.record.User userRecord = userRepository.findByToken(userToken);
        log.info("User with token: {} found", userToken);
        return mapToModel(userRecord);
    }

    @Transactional
    public User addUser(final User user) throws Exception {
        org.deutschebank.auction.users.repository.record.User existingUserByPhone =
                userRepository.findByPhoneNumber(user.getPhoneNumber());
        if (existingUserByPhone != null) {
            log.warn("Phone number: {} already in use", user.getPhoneNumber());
            throw new IllegalArgumentException("This phone number cannot be used");
        }
        org.deutschebank.auction.users.repository.record.User existingUserByEmail =
                userRepository.findByEmail(user.getEmail());
        if (existingUserByEmail != null) {
            log.warn("Email: {} already in use", user.getEmail());
            throw new IllegalArgumentException("This email cannot be used");
        }

        org.deutschebank.auction.users.repository.record.User userRecord = mapToRecord(user);
        org.deutschebank.auction.users.repository.record.User savedUser = userRepository.save(userRecord);
        log.info("New user created with user token: {}", savedUser.getToken());
        return mapToModel(savedUser);
    }

    @Transactional(readOnly = true)
    public User searchUser(final SearchUserRequest searchUserRequest) throws Exception {
        org.deutschebank.auction.users.repository.record.User userRecord = null;
        if (StringUtils.isNotBlank(searchUserRequest.getEmail()) &&
                StringUtils.isNotBlank(searchUserRequest.getPhoneNumber())) {
            userRecord = userRepository.findByPhoneNumberAndEmail(searchUserRequest.getPhoneNumber(),
                    searchUserRequest.getEmail());
        } else if (StringUtils.isNotBlank(searchUserRequest.getPhoneNumber())) {
            userRecord = userRepository.findByPhoneNumber(searchUserRequest.getPhoneNumber());
        } else if (StringUtils.isNotBlank(searchUserRequest.getEmail())) {
            userRecord = userRepository.findByEmail(searchUserRequest.getEmail());
        }
        if (userRecord == null) {
            return null;
        }
        log.info("User found for email: {} and phone number: {}", searchUserRequest.getEmail(),
                searchUserRequest.getPhoneNumber());
        return mapToModel(userRecord);
    }

    private org.deutschebank.auction.users.repository.record.User mapToRecord(User user) throws Exception {
        if (user == null) {
            log.error("Cannot convert user to entity");
            throw new Exception("Unprocessable entity");
        }

        org.deutschebank.auction.users.repository.record.User userRecord =
                new org.deutschebank.auction.users.repository.record.User();
        userRecord.setFirstName(user.getFirstName());
        userRecord.setLastName(user.getLastName());
        userRecord.setPhoneNumber(user.getPhoneNumber());
        userRecord.setEmail(user.getEmail());
        userRecord.setStreet(user.getStreet());
        userRecord.setHouseNumber(user.getHouseNumber());
        userRecord.setCity(user.getCity());
        userRecord.setPincode(user.getPincode());
        userRecord.setAdditionalAddressInfo(user.getAdditionalAddressInfo());
        userRecord.setCountry(user.getCountry());
        return userRecord;
    }

    private User mapToModel(org.deutschebank.auction.users.repository.record.User savedUser) throws Exception {
        if (savedUser == null) {
            log.error("User record is not processable");
            throw new Exception("Unprocessable entity");
        }
        return User.builder()
                .userToken(savedUser.getToken())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .street(savedUser.getStreet())
                .houseNumber(savedUser.getHouseNumber())
                .pincode(savedUser.getPincode())
                .city(savedUser.getCity())
                .additionalAddressInfo(savedUser.getAdditionalAddressInfo())
                .country(savedUser.getCountry())
                .build();
    }

}
