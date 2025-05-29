package com.flash.user.service;

import com.flash.user.domain.User;
import com.flash.user.exception.UserException;
import com.flash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public User createUser(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException.UserAlreadyExistsException();
        }
        
        User user = User.builder()
            .email(email)
            .password(password)  // 실제로는 암호화 필요
            .name(name)
            .build();
            
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(UserException.UserNotFoundException::new);
    }
} 