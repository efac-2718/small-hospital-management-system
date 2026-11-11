package com.example.small_hospital_management_system.controller;

import com.example.small_hospital_management_system.dto.RegisterRequest;
import com.example.small_hospital_management_system.dto.StatsResponse;
import com.example.small_hospital_management_system.dto.UserResponse;
import com.example.small_hospital_management_system.service.AuthService;
import com.example.small_hospital_management_system.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET    /api/users            - ADMIN: every account
 * DELETE /api/users/{id}       - ADMIN (also /api/admin/users/{id})
 * GET    /api/doctors          - any logged-in user (patients choose a doctor here)
 * POST   /api/admin/doctors    - ADMIN: add a new doctor profile
 * GET    /api/admin/stats      - ADMIN: overall hospital statistics
 */
@RestController
@RequiredArgsConstructor
public class StaffController {

    private final AuthService authService;
    private final StatsService statsService;

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listUsers() {
        return authService.listUsers();
    }

    @DeleteMapping({"/api/users/{id}", "/api/admin/users/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id, Authentication authentication) {
        authService.deleteUser(id, authentication.getName());
    }

    @GetMapping("/api/doctors")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponse> listDoctors() {
        return authService.listDoctors();
    }

    @PostMapping("/api/admin/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addDoctor(@Valid @RequestBody RegisterRequest request) {
        return authService.createDoctor(request);
    }

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public StatsResponse stats() {
        return statsService.stats();
    }
}
