package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body for creating an account.
 * <ul>
 *   <li>role is optional and defaults to PATIENT (anyone may self-register as a patient).</li>
 *   <li>Staff roles (ADMIN, DOCTOR, NURSE, RECEPTIONIST) need a valid ADMIN token.</li>
 *   <li>"name" is accepted as an alias of "fullName".</li>
 * </ul>
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9._@/-]+$",
                message = "Username may only contain letters, digits and . _ @ / -")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        @JsonAlias("name")
        String fullName,

        Role role,

        @Size(max = 100, message = "Specialization must be at most 100 characters")
        String specialization) {
}
