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
 * GET    /api/patients[?name=]  - ADMIN, DOCTOR, NURSE, RECEPTIONIST
 * GET    /api/admin/patients    - ADMIN only
 * GET    /api/patients/me       - PATIENT: own profile
 * GET    /api/patients/{id}     - ADMIN, DOCTOR, NURSE, RECEPTIONIST
 * POST   /api/patients          - ADMIN, RECEPTIONIST
 * PUT    /api/patients/{id}     - ADMIN, RECEPTIONIST
 * DELETE /api/patients/{id}     - ADMIN only (also /api/admin/patients/{id}); 409 if the patient has data
 */
@RestController
@RequiredArgsConstructor
public class PatientController {

    private static final String STAFF = "hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')";
    private static final String FRONT_DESK = "hasAnyRole('ADMIN','RECEPTIONIST')";

    private final PatientService patientService;

    @GetMapping("/api/patients")
    @PreAuthorize(STAFF)
    public List<Patient> list(@RequestParam(required = false) String name) {
        return patientService.findAll(name);
    }

    @GetMapping("/api/admin/patients")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Patient> listForAdmin(@RequestParam(required = false) String name) {
        return patientService.findAll(name);
    }

    @GetMapping("/api/patients/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Patient myProfile(Authentication authentication) {
        return patientService.findByUsername(authentication.getName());
    }

    @GetMapping("/api/patients/{id}")
    @PreAuthorize(STAFF)
    public Patient get(@PathVariable String id) {
        return patientService.findById(id);
    }

    @PostMapping("/api/patients")
    @PreAuthorize(FRONT_DESK)
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@Valid @RequestBody Patient patient) {
        return patientService.create(patient);
    }

    @PutMapping("/api/patients/{id}")
    @PreAuthorize(FRONT_DESK)
    public Patient update(@PathVariable String id, @Valid @RequestBody Patient patient) {
        return patientService.update(id, patient);
    }

    @DeleteMapping({"/api/patients/{id}", "/api/admin/patients/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        patientService.delete(id);
    }
}
