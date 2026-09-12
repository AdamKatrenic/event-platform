package com.adam.event_platform.controller;

import com.adam.event_platform.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adam.event_platform.dto.LoginRequest;
import com.adam.event_platform.dto.UserRegistrationRequest;
import com.adam.event_platform.dto.UserResponse;
import com.adam.event_platform.model.User;
import com.adam.event_platform.security.JwtUtils;
import com.adam.event_platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Check if user exists first - return 403 for non-existent users
        if (!userService.existsByUsername(loginRequest.username())) {
            log.debug("User '{}' not found, returning 403 Forbidden", loginRequest.username());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // User exists, now authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            String token = jwtUtils.generateToken(loginRequest.username());
            return ResponseEntity.ok(new AuthResponse(token, loginRequest.username()));
        } catch (BadCredentialsException e) {
            // Wrong password - return 401 Unauthorized
            log.debug("Wrong password for user '{}', returning 401 Unauthorized", loginRequest.username());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        User user = userService.register(registrationRequest);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getCreatedAt()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // Clear the security context by removing authentication
            if (authentication != null) {
                authentication.setAuthenticated(false);
            }
        }
        return ResponseEntity.ok().build();
    }
}