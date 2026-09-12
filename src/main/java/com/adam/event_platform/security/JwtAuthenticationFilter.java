package com.adam.event_platform.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final com.adam.event_platform.service.UserService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, com.adam.event_platform.service.UserService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Clear any existing authentication to ensure fresh auth check for each request
        SecurityContextHolder.clearContext();

        String header = request.getHeader("Authorization");

        // Only try to authenticate if there's a Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            String jwt = header.substring(7);
            
            try {
                // Try to extract username from token
                String usernameFromToken = jwtUtils.getUsernameFromToken(jwt);

                // Only proceed if we got a valid non-empty username
                if (usernameFromToken == null || usernameFromToken.isEmpty()) {
                    log.warn("Invalid token provided - username extraction returned null or empty");
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                    return;
                } else {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);

                    if (userDetails != null) {
                        try {
                            java.util.Date expiration = jwtUtils.getClaimFromToken(jwt, io.jsonwebtoken.Claims::getExpiration);
                            boolean isExpired = new Date().after(expiration);
                            
                            if (isExpired) {
                                log.warn("Token has expired for user: {}", usernameFromToken);
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
                                return; // Stop filter chain on expired token
                            }

                            boolean isTokenValid = jwtUtils.isTokenValid(jwt, userDetails);
                            
                            if (userDetails != null && isTokenValid) {
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("Authentication successful for user: {}", usernameFromToken);
                            } else {
                                // Token is invalid (e.g., wrong signature) or user doesn't match
                                log.warn("Authentication failed for user: {}", usernameFromToken);
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token or insufficient privileges");
                                return; // Stop filter chain on invalid token
                            }
                        } catch (Exception e) {
                            log.error("Authentication error for user: {}", usernameFromToken, e);
                            SecurityContextHolder.clearContext();
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token or insufficient privileges");
                            return; // Stop filter chain on auth errors
                        }
                    } else {
                        log.debug("No user found in database for username: {}", usernameFromToken);
                        // Don't set auth, continue filter chain
                    }
                }
            } catch (Exception e) {
                log.error("Error processing token", e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                return;
            }
        }

        // Continue filter chain for all requests (auth not required or handled above)
        filterChain.doFilter(request, response);
    }
}