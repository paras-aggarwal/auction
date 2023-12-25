package org.deutschebank.auction.users.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.users.exception.InvalidRequestException;
import org.deutschebank.auction.users.exception.ResourceNotFoundException;
import org.deutschebank.auction.users.model.User;
import org.deutschebank.auction.users.model.request.SearchUserRequest;
import org.deutschebank.auction.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUser(final String userToken) throws Exception {
        final org.deutschebank.auction.users.repository.record.User userRecord = userRepository.findByToken(userToken);
        if (userRecord == null) {
            log.warn("User not found for the given token: {}", userToken);
            throw new ResourceNotFoundException("", "User not found");
        }
        log.info("User with token: {} found", userToken);
        return mapToModel(userRecord);
    }

    @Transactional
    public User addUser(final User user) throws Exception {
        checkForExistingUser(user);
        final org.deutschebank.auction.users.repository.record.User userRecord = mapToRecord(user);
        try {
            org.deutschebank.auction.users.repository.record.User savedUser = userRepository.save(userRecord);
            log.info("New user created with user token: {}", savedUser.getToken());
            return mapToModel(savedUser);
        } catch (final Exception e) {
            log.error("Exception occurred while creating user: ", e);
            throw e;
        }
    }

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
            throw new ResourceNotFoundException("", "User not found for the provided information");
        }
        log.info("User found for email: {} and phone number: {}", searchUserRequest.getEmail(),
                searchUserRequest.getPhoneNumber());
        return mapToModel(userRecord);
    }

    private void checkForExistingUser(final User user) {
        final org.deutschebank.auction.users.repository.record.User existingUserByPhone =
                userRepository.findByPhoneNumber(user.getPhoneNumber());
        if (existingUserByPhone != null) {
            log.warn("Phone number: {} already in use", user.getPhoneNumber());
            throw new InvalidRequestException("", "Phone number is already associated with an account");
        }
        final org.deutschebank.auction.users.repository.record.User existingUserByEmail =
                userRepository.findByEmail(user.getEmail());
        if (existingUserByEmail != null) {
            log.warn("Email: {} already in use", user.getEmail());
            throw new InvalidRequestException("", "Email is already associated with an account");
        }
    }

    private org.deutschebank.auction.users.repository.record.User mapToRecord(final User user) throws Exception {
        if (user == null) {
            log.error("Cannot convert user to entity");
            throw new Exception("Unprocessable entity");
        }

        final org.deutschebank.auction.users.repository.record.User userRecord =
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

    private User mapToModel(final org.deutschebank.auction.users.repository.record.User savedUser) throws Exception {
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
