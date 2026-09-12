package com.adam.event_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // Check for authentication directly - return 401 if no valid auth context exists
        var securityContext = SecurityContextHolder.getContext();
        
        // No security context at all
        if (securityContext == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        Authentication auth = securityContext.getAuthentication();
        
        // No authentication object
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        // Anonymous user - treat as not authenticated
        if (auth.getName() != null && auth.getName().startsWith("anonymous")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        // Authentication principal is null or empty
        if (auth.getName() == null || auth.getAuthorities().isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        // Check if authenticated user has required role
        var roles = auth.getAuthorities().stream()
                .map(userRole -> userRole.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        
        if (!roles.contains("USER") && !roles.contains("ADMIN")) {
            // Has auth but doesn't have USER or ADMIN role - return 403
            return ResponseEntity.status(403).body(Map.of("error", "Access denied. Insufficient permissions"));
        }

        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "roles", roles
        ));
    }
}