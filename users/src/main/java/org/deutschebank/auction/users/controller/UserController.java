package org.deutschebank.auction.users.controller;

import lombok.RequiredArgsConstructor;
import org.deutschebank.auction.users.model.User;
import org.deutschebank.auction.users.model.request.SearchUserRequest;
import org.deutschebank.auction.users.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/{userToken}")
    public User getUserDetails(@PathVariable("userToken") String userToken) throws Exception {
        return userService.getUser(userToken);
    }

    @PostMapping("/user")
    public User addUser(@RequestBody User user) throws Exception {
        return userService.addUser(user);
    }

    @PostMapping("/user/search")
    public User searchUser(@RequestBody SearchUserRequest searchUserRequest) throws Exception {
        return userService.searchUser(searchUserRequest);
    }

}
