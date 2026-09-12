package com.adam.event_platform.controller;

import com.adam.event_platform.dto.UserResponse;
import com.adam.event_platform.model.User;
import com.adam.event_platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        // Check for authentication directly - return 401 if no valid auth context exists
        var securityContext = SecurityContextHolder.getContext();
        
        // No security context at all
        if (securityContext == null) {
            return ResponseEntity.status(401).body(List.of());
        }

        Authentication auth = securityContext.getAuthentication();
        
        // No authentication object
        if (auth == null) {
            return ResponseEntity.status(401).body(List.of());
        }
        
        // Anonymous user - treat as not authenticated
        if (auth.getName() != null && auth.getName().startsWith("anonymous")) {
            return ResponseEntity.status(401).body(List.of());
        }
        
        // Authentication principal is null or empty
        if (auth.getName() == null || auth.getAuthorities().isEmpty()) {
            return ResponseEntity.status(401).body(List.of());
        }

        // Check if authenticated user has ADMIN role
        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        
        if (!authorities.contains("ROLE_ADMIN")) {
            // Has auth but doesn't have ADMIN role - return 403
            return ResponseEntity.status(403).body(List.of());
        }

        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{id}/promote")
    public ResponseEntity<String> promoteUserToAdmin(@PathVariable Long id) {
        // Check for authentication directly - return 401 if no valid auth context exists
        var securityContext = SecurityContextHolder.getContext();
        
        // No security context at all
        if (securityContext == null) {
            return ResponseEntity.status(401).body("Access denied");
        }

        Authentication auth = securityContext.getAuthentication();
        
        // No authentication object
        if (auth == null) {
            return ResponseEntity.status(401).body("Access denied");
        }
        
        // Anonymous user - treat as not authenticated
        if (auth.getName() != null && auth.getName().startsWith("anonymous")) {
            return ResponseEntity.status(401).body("Access denied");
        }
        
        // Authentication principal is null or empty
        if (auth.getName() == null || auth.getAuthorities().isEmpty()) {
            return ResponseEntity.status(401).body("Access denied");
        }

        // Check if authenticated user has ADMIN role
        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        
        if (!authorities.contains("ROLE_ADMIN")) {
            // Has auth but doesn't have ADMIN role - return 403
            return ResponseEntity.status(403).body("Access denied");
        }

        userService.promoteToAdmin(id);
        return ResponseEntity.ok("User " + id + " has been promoted to ADMIN.");
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getCreatedAt()
        );
    }
}