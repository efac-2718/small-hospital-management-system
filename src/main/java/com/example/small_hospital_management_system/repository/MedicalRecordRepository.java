package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {

    /** Records of one patient, newest first. */
    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(String patientId);

    /** Does the patient have any medical record? (blocks deleting the patient) */
    boolean existsByPatientId(String patientId);

    boolean existsByDoctorId(String doctorId);
}
