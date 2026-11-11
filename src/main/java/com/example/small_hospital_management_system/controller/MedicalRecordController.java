package com.example.small_hospital_management_system.controller;

import com.example.small_hospital_management_system.dto.MedicalRecordRequest;
import com.example.small_hospital_management_system.dto.MedicalRecordResponse;
import com.example.small_hospital_management_system.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * POST /api/records                     - DOCTOR (also /api/doctors/records); author = doctor in the JWT
 * GET  /api/records/patient/{patientId} - ADMIN, DOCTOR, NURSE, PATIENT (own only)
 * GET  /api/records/{id}                - ADMIN, DOCTOR, NURSE, PATIENT (own only)
 * GET  /api/patients/records            - PATIENT: own records
 */
@RestController
@RequiredArgsConstructor
public class MedicalRecordController {

    private static final String CLINICAL_OR_OWNER = "hasAnyRole('ADMIN','DOCTOR','NURSE','PATIENT')";

    private final MedicalRecordService medicalRecordService;

    @PostMapping({"/api/records", "/api/doctors/records"})
    @PreAuthorize("hasRole('DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponse create(@Valid @RequestBody MedicalRecordRequest request,
                                        Authentication authentication) {
        return medicalRecordService.create(request, authentication.getName());
    }

    @GetMapping("/api/records/patient/{patientId}")
    @PreAuthorize(CLINICAL_OR_OWNER)
    public List<MedicalRecordResponse> forPatient(@PathVariable Long patientId, Authentication authentication) {
        return medicalRecordService.listForPatient(patientId, authentication.getName());
    }

    @GetMapping("/api/records/{id}")
    @PreAuthorize(CLINICAL_OR_OWNER)
    public MedicalRecordResponse get(@PathVariable Long id, Authentication authentication) {
        return medicalRecordService.get(id, authentication.getName());
    }

    @GetMapping("/api/patients/records")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalRecordResponse> myRecords(Authentication authentication) {
        return medicalRecordService.listMine(authentication.getName());
    }
}
