package com.adam.event_platform.security;

/**
 * Application roles. Stored in the database by name (USER, ADMIN) and exposed
 * to Spring Security with the conventional ROLE_ prefix, e.g. ROLE_ADMIN.
 * This is the single source of truth for all roles in the platform.
 */
public enum Role {

    USER,
    ADMIN;

    /** Spring Security authority form of this role, e.g. ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
