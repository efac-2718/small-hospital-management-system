package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        String doctorSpecialization,
        LocalDateTime appointmentTime,
        String reason,
        AppointmentStatus status,
        LocalDateTime createdAt) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getDoctor().getSpecialization(),
                a.getAppointmentTime(),
                a.getReason(),
                a.getStatus(),
                a.getCreatedAt());
    }
}
