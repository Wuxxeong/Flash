package com.flash.user.controller;

import com.flash.user.domain.User;
import com.flash.user.dto.UserResponse;
import com.flash.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestParam String email, 
                                             @RequestParam String password,
                                             @RequestParam String name) {
        User user = userService.createUser(email, password, name);
        return ResponseEntity.ok(UserResponse.from(user));
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserResponse.from(user));
    }
} 