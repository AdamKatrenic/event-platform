package com.adam.event_platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final com.adam.event_platform.service.UserService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, com.adam.event_platform.service.UserService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String jwt = header.substring(7);
            try {
                String usernameFromToken = jwtUtils.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);

                System.out.println("--- JWT DEBUG START ---");
                System.out.println("1. Token Username: " + usernameFromToken);
                if (userDetails != null) {
                    System.out.println("2. DB User Found: " + userDetails.getUsername());
                    System.out.println("3. DB Authorities: " + userDetails.getAuthorities());
                    
                    boolean isSameUser = usernameFromToken.equals(userDetails.getUsername());
                    System.out.println("4. Username Match? " + isSameUser);

                    // Check expiration manually for extra info
                    try {
                        java.util.Date expiration = jwtUtils.getClaimFromToken(jwt, io.jsonwebtoken.Claims::getExpiration);
                        boolean isExpired = new java.util.Date().after(expiration);
                        System.out.println("5. Token Expired? " + isExpired);
                    } catch (Exception e) {
                        System.out.println("5. Could not check expiration: " + e.getMessage());
                    }

                    if (userDetails != null && jwtUtils.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("RESULT: Authentication SUCCESSFUL");
                    } else {
                        System.out.println("RESULT: Authentication FAILED (isTokenValid returned false)");
                    }
                } else {
                    System.out.println("2. DB User Found? NO");
                }
                System.out.println("--- JWT DEBUG END ---");

            } catch (Exception e) {
                log.error("Cannot set user authentication", e);
                System.out.println("ERROR: Exception during token processing: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}