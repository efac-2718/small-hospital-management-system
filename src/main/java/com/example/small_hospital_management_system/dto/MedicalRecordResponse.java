package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.MedicalRecord;

import java.time.LocalDateTime;

public record MedicalRecordResponse(
        String id,
        String patientId,
        String patientName,
        String doctorId,
        String doctorName,
        String appointmentId,
        String diagnosis,
        String prescription,
        String notes,
        LocalDateTime createdAt) {

    public static MedicalRecordResponse from(MedicalRecord r) {
        return new MedicalRecordResponse(
                r.getId(),
                r.getPatientId(),
                r.getPatientName(),
                r.getDoctorId(),
                r.getDoctorName(),
                r.getAppointmentId(),
                r.getDiagnosis(),
                r.getPrescription(),
                r.getNotes(),
                r.getCreatedAt());
    }
}
