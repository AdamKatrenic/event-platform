package com.adam.event_platform.controller;

import com.adam.event_platform.dto.CreateEventRequest;
import com.adam.event_platform.dto.LoginRequest;
import com.adam.event_platform.dto.UserRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class EventControllerTest {

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
                "eventuser", "password123456", "event@example.com", false
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("eventuser", "password123456");
        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        
        authToken = objectMapper.readTree(tokenResponse).get("token").asText();
    }

    @Test
    void testCreateEvent_ShouldReturnCreated() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "Test Event",
                "This is a test event description",
                10,
                LocalDateTime.now().plusDays(7)
        );

        mockMvc.perform(post("/api/event")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Event"))
            .andExpect(jsonPath("$.capacity").value(10));
    }

    @Test
    void testGetEvents_ShouldReturnAllEvents() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "Another Event",
                "Another description",
                5,
                LocalDateTime.now().plusDays(3)
        );

        mockMvc.perform(post("/api/event")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").value("Another Event"));
    }

    @Test
    void testGetEventByTitle_ShouldReturnEvent() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "Specific Event",
                "Event for testing retrieval",
                20,
                LocalDateTime.now().plusDays(5)
        );

        mockMvc.perform(post("/api/event")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        String title = "Specific Event";
        mockMvc.perform(get("/api/events/" + title))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void testDeleteEvent_ShouldReturnOk() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "ToBeDeleted Event",
                "This will be deleted",
                15,
                LocalDateTime.now().plusDays(10)
        );

        mockMvc.perform(post("/api/event")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        String title = "ToBeDeleted Event";
        mockMvc.perform(delete("/api/events/" + title))
            .andExpect(status().isOk());
    }

    @Test
    void testUpdateEvent_ShouldReturnOk() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "ToUpdate Event",
                "Original description",
                100,
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/api/event")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        String title = "ToUpdate Event";
        
        CreateEventRequest updateRequest = new CreateEventRequest(
                "Updated Title",
                "Updated description",
                50,
                LocalDateTime.now().plusDays(1)
        );

        mockMvc.perform(put("/api/events/" + title)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testGetEventByTitle_WithInvalidTitle_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/events/NonExistent Event"))
            .andExpect(status().isNotFound());
    }
}