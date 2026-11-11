package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        String patientId,
        String patientName,
        String doctorId,
        String doctorName,
        String doctorSpecialization,
        LocalDateTime appointmentTime,
        String reason,
        AppointmentStatus status,
        LocalDateTime createdAt) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatientId(),
                a.getPatientName(),
                a.getDoctorId(),
                a.getDoctorName(),
                a.getDoctorSpecialization(),
                a.getAppointmentTime(),
                a.getReason(),
                a.getStatus(),
                a.getCreatedAt());
    }
}
