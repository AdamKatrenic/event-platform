package com.adam.event_platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.adam.event_platform.dto.*;
import com.adam.event_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String authToken;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Clear database and register test user
        userRepository.deleteAll();
        
        UserRegistrationRequest registration = new UserRegistrationRequest(
                "testuser", "password123456", "test@example.com", false
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("testuser", "password123456");
        String token = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        
        authToken = objectMapper.readTree(token).get("token").asText();
    }

    @Test
    void testRegisterUser_ShouldReturnCreated() throws Exception {
        UserRegistrationRequest registration = new UserRegistrationRequest(
                "newuser", "password123456", "new@example.com", false
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void testRegisterUser_WithExistingUser_ShouldReturnConflict() throws Exception {
        UserRegistrationRequest registration = new UserRegistrationRequest(
                "testuser", "differentpassword", "other@example.com", false
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isConflict());
    }

    @Test
    void testLogin_ShouldReturnAuthToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_WithNullUser_ShouldReturnForbidden() throws Exception {
        LoginRequest loginRequest = new LoginRequest("null", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testLogout_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk());
    }
}