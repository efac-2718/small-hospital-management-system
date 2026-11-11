package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient, String> {

    /** Search patients by (part of) their name, ignoring case: a case-insensitive regex query. */
    List<Patient> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(String name);

    List<Patient> findAllByOrderByFullNameAsc();

    /** The patient profile linked to a PATIENT login account. */
    Optional<Patient> findByUsername(String username);
}
