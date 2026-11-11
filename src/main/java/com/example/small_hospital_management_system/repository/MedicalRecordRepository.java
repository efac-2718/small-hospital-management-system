package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /** Records of one patient, newest first. */
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /** Does the patient have any medical record? (blocks deleting the patient) */
    boolean existsByPatientId(Long patientId);

    boolean existsByDoctorId(Long doctorId);
}
