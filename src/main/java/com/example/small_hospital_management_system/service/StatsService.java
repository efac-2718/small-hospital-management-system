package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.dto.StatsResponse;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.MedicalRecordRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import com.example.small_hospital_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Overall hospital statistics for the ADMIN. */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        return new StatsResponse(
                patientRepository.count(),
                userRepository.countByRole(Role.DOCTOR),
                userRepository.countByRole(Role.NURSE),
                userRepository.countByRole(Role.RECEPTIONIST),
                userRepository.countByRole(Role.ADMIN),
                appointmentRepository.count(),
                appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED),
                appointmentRepository.countByStatus(AppointmentStatus.COMPLETED),
                appointmentRepository.countByStatus(AppointmentStatus.CANCELLED),
                medicalRecordRepository.count());
    }
}
