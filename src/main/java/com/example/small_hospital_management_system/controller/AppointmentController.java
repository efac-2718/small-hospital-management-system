package com.example.small_hospital_management_system.controller;

import com.example.small_hospital_management_system.dto.AppointmentRequest;
import com.example.small_hospital_management_system.dto.AppointmentResponse;
import com.example.small_hospital_management_system.dto.StatusRequest;
import com.example.small_hospital_management_system.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * POST  /api/appointments              - ADMIN, RECEPTIONIST, NURSE, PATIENT (patients book for themselves)
 * POST  /api/patients/appointments     - PATIENT: book a doctor appointment
 * GET   /api/appointments              - any logged-in user (doctors/patients only see their own)
 * GET   /api/doctors/appointments      - DOCTOR (own), ADMIN (all)
 * GET   /api/patients/appointments     - PATIENT: own appointments
 * GET   /api/appointments/{id}         - any logged-in user, ownership enforced
 * PATCH /api/appointments/{id}/status  - ADMIN, RECEPTIONIST, DOCTOR (own), PATIENT (cancel own)
 */
@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/api/appointments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','NURSE','PATIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        return appointmentService.book(request, authentication.getName());
    }

    @PostMapping("/api/patients/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse bookAsPatient(@Valid @RequestBody AppointmentRequest request,
                                             Authentication authentication) {
        return appointmentService.book(request, authentication.getName());
    }

    @GetMapping("/api/appointments")
    @PreAuthorize("isAuthenticated()")
    public List<AppointmentResponse> list(Authentication authentication) {
        return appointmentService.listVisibleTo(authentication.getName());
    }

    @GetMapping("/api/doctors/appointments")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<AppointmentResponse> doctorAppointments(Authentication authentication) {
        return appointmentService.listVisibleTo(authentication.getName());
    }

    @GetMapping("/api/patients/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> myAppointments(Authentication authentication) {
        return appointmentService.listVisibleTo(authentication.getName());
    }

    @GetMapping("/api/appointments/{id}")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse get(@PathVariable String id, Authentication authentication) {
        return appointmentService.get(id, authentication.getName());
    }

    @RequestMapping(value = "/api/appointments/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','PATIENT')")
    public AppointmentResponse updateStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request,
                                            Authentication authentication) {
        return appointmentService.updateStatus(id, request.status(), authentication.getName());
    }
}
