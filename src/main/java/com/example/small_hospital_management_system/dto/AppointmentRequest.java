package com.example.small_hospital_management_system.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Body for booking an appointment.
 * patientId is ignored for PATIENT users (they always book for themselves) and required for staff.
 */
public record AppointmentRequest(
        String patientId,

        @NotBlank(message = "doctorId is required")
        String doctorId,

        @NotNull(message = "appointmentTime is required")
        @Future(message = "appointmentTime must be in the future")
        LocalDateTime appointmentTime,

        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason) {
}
