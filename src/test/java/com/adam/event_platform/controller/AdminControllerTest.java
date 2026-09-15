package com.adam.event_platform.controller;

import com.adam.event_platform.dto.LoginRequest;
import com.adam.event_platform.dto.UserRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.adam.event_platform.model.User;
import com.adam.event_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String regularUserToken;
    private String adminToken;
    private long regularUserId;
    private long adminUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Clear database before each test
        userRepository.deleteAll();
        
        UserRegistrationRequest registration = new UserRegistrationRequest(
                "regularuser", "password123456", "regular@example.com", false
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated());

        regularUserToken = getAuthToken("regularuser");

        UserRegistrationRequest adminReg = new UserRegistrationRequest(
                "adminuser", "password123456", "admin@example.com", true
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminReg)))
            .andExpect(status().isCreated());

        adminToken = getAuthToken("adminuser");
        
        // Get actual user IDs after creation (needed for promote tests)
        List<User> users = userRepository.findAll();
        Map<String, Long> userIds = users.stream()
                .collect(java.util.stream.Collectors.toMap(u -> u.getUsername(), User::getId));
        regularUserId = userIds.get("regularuser");
        adminUserId = userIds.get("adminuser");
    }

    private String getAuthToken(String username) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, "password123456");
        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        
        return objectMapper.readTree(tokenResponse).get("token").asText();
    }

    @Test
    void testListUsers_AsRegularUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + regularUserToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void testListUsers_AsAdmin_ShouldReturnOkWithAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].username").value("regularuser"))
            .andExpect(jsonPath("$[1].username").value("adminuser"));
    }

    @Test
    void testListUsers_WithoutAuth_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void testPromoteUser_ToAdmin_AsRegularUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + regularUserId + "/promote")
                .header("Authorization", "Bearer " + regularUserToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void testPromoteUser_ToAdmin_AsAdmin_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + adminUserId + "/promote")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(content().string("User " + adminUserId + " has been promoted to ADMIN."));
    }

    @Test
    void testPromoteUser_ToAdmin_WithoutAuth_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + regularUserId + "/promote"))
            .andExpect(status().isForbidden());
    }

    @Test
    void testPromoteUser_ToAdmin_WithInvalidRole_ShouldReturnForbidden() throws Exception {
        // First create a user to promote
        UserRegistrationRequest registration = new UserRegistrationRequest(
                "tobepromote", "password123456", "to@promote.com", false
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated());

        // Login as admin to promote
        adminToken = getAuthToken("adminuser");

        // Find the new user ID
        List<User> users = userRepository.findAll();
        long userToPromoteId = users.stream()
                .filter(u -> u.getUsername().equals("tobepromote"))
                .findFirst()
                .get()
                .getId();

        mockMvc.perform(post("/api/v1/admin/users/" + userToPromoteId + "/promote")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(content().string("User " + userToPromoteId + " has been promoted to ADMIN."));
    }
}