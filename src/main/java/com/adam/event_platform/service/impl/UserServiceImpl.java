package com.adam.event_platform.service.impl;

import com.adam.event_platform.dto.UserRegistrationRequest;
import com.adam.event_platform.exception.ResourceNotFoundException;
import com.adam.event_platform.exception.UserAlreadyExistsException;
import com.adam.event_platform.model.User;
import com.adam.event_platform.repository.UserRepository;
import com.adam.event_platform.security.Role;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements com.adam.event_platform.service.UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @CacheEvict(value = "users", key = "'all'", allEntries = true)
    public User register(UserRegistrationRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        // Default role: every new account starts as a regular USER
        user.getRoles().add(Role.USER);
        // If admin flag is set, also assign ADMIN role
        if (Boolean.TRUE.equals(request.isAdmin())) {
            user.getRoles().add(Role.ADMIN);
        }

        return userRepository.save(user);
    }

    @Override
    @CacheEvict(value = "users", key = "'all'", allEntries = true)
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        user.getRoles().add(Role.ADMIN);
        userRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new com.adam.event_platform.security.CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.authority()))
                        .collect(Collectors.toList())
        );
    }
}