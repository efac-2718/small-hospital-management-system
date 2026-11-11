package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "status is required (SCHEDULED, COMPLETED or CANCELLED)")
        AppointmentStatus status) {
}
