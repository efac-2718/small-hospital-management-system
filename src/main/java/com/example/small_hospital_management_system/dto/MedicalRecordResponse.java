package com.example.small_hospital_management_system.dto;

import com.example.small_hospital_management_system.model.MedicalRecord;

import java.time.LocalDateTime;

public record MedicalRecordResponse(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        Long appointmentId,
        String diagnosis,
        String prescription,
        String notes,
        LocalDateTime createdAt) {

    public static MedicalRecordResponse from(MedicalRecord r) {
        return new MedicalRecordResponse(
                r.getId(),
                r.getPatient().getId(),
                r.getPatient().getFullName(),
                r.getDoctor().getId(),
                r.getDoctor().getFullName(),
                r.getAppointment() == null ? null : r.getAppointment().getId(),
                r.getDiagnosis(),
                r.getPrescription(),
                r.getNotes(),
                r.getCreatedAt());
    }
}
