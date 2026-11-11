package com.example.small_hospital_management_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Body for writing a medical record. The doctor is taken from the JWT, not from this body. */
public record MedicalRecordRequest(
        @NotNull(message = "patientId is required")
        Long patientId,

        Long appointmentId,

        @NotBlank(message = "Diagnosis is required")
        @Size(max = 1000, message = "Diagnosis must be at most 1000 characters")
        String diagnosis,

        @Size(max = 1000, message = "Prescription must be at most 1000 characters")
        String prescription,

        @Size(max = 2000, message = "Notes must be at most 2000 characters")
        String notes) {
}
