package com.adam.event_platform.controller;

import com.adam.event_platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteUserToAdmin(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok("User " + id + " has been promoted to ADMIN.");
    }
}