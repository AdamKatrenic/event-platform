package com.adam.event_platform.service;

import com.adam.event_platform.dto.UserRegistrationRequest;
import com.adam.event_platform.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

public interface UserService extends UserDetailsService {
    User register(UserRegistrationRequest request);
}
