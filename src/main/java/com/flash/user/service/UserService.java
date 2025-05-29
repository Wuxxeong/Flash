package com.flash.user.service;

import com.flash.user.domain.User;

public interface UserService {
    User createUser(String email, String password, String name);
    User getUserByEmail(String email);
}