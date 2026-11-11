package com.example.small_hospital_management_system.dto;

/** Overall hospital statistics for the ADMIN dashboard (GET /api/admin/stats). */
public record StatsResponse(
        long totalPatients,
        long totalDoctors,
        long totalNurses,
        long totalReceptionists,
        long totalAdmins,
        long totalAppointments,
        long scheduledAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long totalMedicalRecords) {
}
