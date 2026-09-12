package com.adam.event_platform;

import com.adam.event_platform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        // Public endpoints - no authentication required
                        .requestMatchers("/", "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        // Event endpoints are public
                        .requestMatchers("/api/event", "/api/events/**").permitAll()
                        // Users endpoint: requires no authentication at filter level, controller handles auth check
                        .requestMatchers("/api/v1/users/me").permitAll()
                        // Admin endpoints require authentication at filter level
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/users")
                        .hasAnyRole("ADMIN", "USER") // Allow USER role to list users
                        .requestMatchers("/api/v1/admin/users/**")
                        .hasAnyRole("ADMIN") // Admin can only promote users
                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}