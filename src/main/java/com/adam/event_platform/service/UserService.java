package com.adam.event_platform.service;

import com.adam.event_platform.dto.UserRegistrationRequest;
import com.adam.event_platform.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User register(UserRegistrationRequest request);
    void promoteToAdmin(Long userId);
    List<User> getAllUsers();
    boolean existsByUsername(String username);
}
