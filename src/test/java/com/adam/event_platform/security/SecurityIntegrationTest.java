package com.adam.event_platform.security;

import com.adam.event_platform.dto.AuthResponse;
import com.adam.event_platform.dto.LoginRequest;
import com.adam.event_platform.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegisterAndLoginFlow() {
        // 1. Register a new user
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                "testuser",
                "password123456",
                "test@example.com"
        );

        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", registrationRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Login with the registered user
        LoginRequest loginRequest = new LoginRequest("testuser", "password123456");
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, AuthResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().token()).isNotEmpty();
        assertThat(loginResponse.getBody().username()).isEqualTo("testuser");
    }
}