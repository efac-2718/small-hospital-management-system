package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

/** A user as shown to clients. Deliberately has no password field. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        String id,
        String username,
        String fullName,
        Role role,
        String specialization) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getRole(), user.getSpecialization());
    }
}
