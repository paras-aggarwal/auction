package org.deutschebank.auction.users.service;

import org.assertj.core.api.Assertions;
import org.deutschebank.auction.users.exception.InvalidRequestException;
import org.deutschebank.auction.users.exception.ResourceNotFoundException;
import org.deutschebank.auction.users.model.request.SearchUserRequest;
import org.deutschebank.auction.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenValidUserToken_whenGetUser_thenReturnUserObject() throws Exception {
        org.deutschebank.auction.users.model.User userModel = getUserModel();
        org.deutschebank.auction.users.model.User addUserResponse = userService.addUser(userModel);
        org.deutschebank.auction.users.model.User response = userService.getUser(addUserResponse.getUserToken());

        Assertions.assertThat(response.getUserToken()).isEqualTo(addUserResponse.getUserToken());
        Assertions.assertThat(response.getPhoneNumber()).isEqualTo(addUserResponse.getPhoneNumber());
        Assertions.assertThat(response.getEmail()).isEqualTo(addUserResponse.getEmail());
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenInvalidUserToken_whenGetUser_thenThrowException() throws Exception {
        org.deutschebank.auction.users.model.User userModel = getUserModel();
        userService.addUser(userModel);
        assertThrows(InvalidRequestException.class, () -> userService.getUser(UUID.randomUUID().toString()));
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenNonExistingUser_whenAddUser_thenReturnSavedUser() throws Exception {
        org.deutschebank.auction.users.model.User user = getUserModel();
        org.deutschebank.auction.users.model.User response = userService.addUser(user);
        Assertions.assertThat(response.getUserToken()).isNotNull();
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenExistingUser_whenAddUser_thenThrowException() throws Exception {
        org.deutschebank.auction.users.model.User user = getUserModel();
        userService.addUser(user);
        assertThrows(InvalidRequestException.class, () -> userService.addUser(user));     // 2nd attempt to add same user
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUserInRecord_whenSearchUser_thenReturnUser() throws Exception {
        org.deutschebank.auction.users.model.User userModel = getUserModel();
        userService.addUser(userModel);

        org.deutschebank.auction.users.model.User response = userService.searchUser(SearchUserRequest.builder()
                .phoneNumber(userModel.getPhoneNumber())
                .email(userModel.getEmail())
                .build());

        Assertions.assertThat(response.getUserToken()).isNotNull();
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUserInRecord_whenSearchUserByPhoneNumber_thenReturnUser() throws Exception {
        org.deutschebank.auction.users.model.User userModel = getUserModel();
        userService.addUser(userModel);

        org.deutschebank.auction.users.model.User response = userService.searchUser(SearchUserRequest.builder()
                .phoneNumber(userModel.getPhoneNumber())
                .build());

        Assertions.assertThat(response.getUserToken()).isNotNull();
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUserInRecord_whenSearchUserByEmail_thenReturnUser() throws Exception {
        org.deutschebank.auction.users.model.User userModel = getUserModel();
        userService.addUser(userModel);

        org.deutschebank.auction.users.model.User response = userService.searchUser(SearchUserRequest.builder()
                .email(userModel.getEmail())
                .build());

        Assertions.assertThat(response.getUserToken()).isNotNull();
    }

    @Test
    @Sql(scripts = "/sql/delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenUserNotInRecord_whenSearchUser_thenReturnNull() {
        assertThrows(ResourceNotFoundException.class, () -> userService.searchUser(SearchUserRequest.builder()
                .phoneNumber("randomPhone")
                .email("abc@xyz.de")
                .build()));
    }

    private org.deutschebank.auction.users.model.User getUserModel() {
        return org.deutschebank.auction.users.model.User.builder()
                .userToken(UUID.randomUUID().toString())
                .firstName("vorname")
                .lastName("nachname")
                .phoneNumber("personalPhone")
                .email("abc@xyz.de")
                .street("randon strasse")
                .houseNumber("1A")
                .city("berlin")
                .pincode(10178L)
                .country("Deutschland")
                .build();
    }

}
