package com.adam.event_platform.controller;

import com.adam.event_platform.dto.AuthResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        String token = jwtUtils.generateToken(loginRequest.username());
        return ResponseEntity.ok(new AuthResponse(token, loginRequest.username()));
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
}
