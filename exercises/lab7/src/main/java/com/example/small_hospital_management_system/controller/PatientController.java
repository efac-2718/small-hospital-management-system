package com.example.small_hospital_management_system.controller;

import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * LAB 7 - Role-Based Access Control with @PreAuthorize.
 *
 * Every method below is missing its @PreAuthorize annotation, so right now ANY logged-in user
 * (even a PATIENT) can list, edit and delete every patient. Add one annotation per TODO so that:
 *
 * GET    /api/patients[?name=]  - ADMIN, DOCTOR, NURSE, RECEPTIONIST
 * GET    /api/admin/patients    - ADMIN only
 * GET    /api/patients/me       - PATIENT: own profile
 * GET    /api/patients/{id}     - ADMIN, DOCTOR, NURSE, RECEPTIONIST
 * POST   /api/patients          - ADMIN, RECEPTIONIST
 * PUT    /api/patients/{id}     - ADMIN, RECEPTIONIST
 * DELETE /api/patients/{id}     - ADMIN only (also /api/admin/patients/{id}); 409 if the patient has data
 *
 * Syntax:  @PreAuthorize("hasRole('ADMIN')")
 *          @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
 * hasRole('ADMIN') checks for the authority "ROLE_ADMIN" (see Lab 2).
 *
 * Questions:
 *  - /api/admin/** is already ADMIN-only in SecurityConfig. Why also annotate listForAdmin()? (defence in depth)
 *  - A wrong role gives 403, a missing/invalid token gives 401. Why are these different status codes?
 *
 * Check your work:   ./mvnw test -Dtest=SecurityFlowIntegrationTests
 */
@RestController
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/api/patients")
    // TODO Lab 7: @PreAuthorize(...)
    public List<Patient> list(@RequestParam(required = false) String name) {
        return patientService.findAll(name);
    }

    @GetMapping("/api/admin/patients")
    // TODO Lab 7: @PreAuthorize(...)
    public List<Patient> listForAdmin(@RequestParam(required = false) String name) {
        return patientService.findAll(name);
    }

    @GetMapping("/api/patients/me")
    // TODO Lab 7: @PreAuthorize(...)
    public Patient myProfile(Authentication authentication) {
        return patientService.findByUsername(authentication.getName());
    }

    @GetMapping("/api/patients/{id}")
    // TODO Lab 7: @PreAuthorize(...)
    public Patient get(@PathVariable String id) {
        return patientService.findById(id);
    }

    @PostMapping("/api/patients")
    // TODO Lab 7: @PreAuthorize(...)
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@Valid @RequestBody Patient patient) {
        return patientService.create(patient);
    }

    @PutMapping("/api/patients/{id}")
    // TODO Lab 7: @PreAuthorize(...)
    public Patient update(@PathVariable String id, @Valid @RequestBody Patient patient) {
        return patientService.update(id, patient);
    }

    @DeleteMapping({"/api/patients/{id}", "/api/admin/patients/{id}"})
    // TODO Lab 7: @PreAuthorize(...)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        patientService.delete(id);
    }
}
