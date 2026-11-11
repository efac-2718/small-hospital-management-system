package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.Role;

/** Login result: the JWT plus who it belongs to. */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        String username,
        String fullName,
        Role role) {
}
