package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** Search patients by (part of) their name, ignoring case. */
    List<Patient> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(String name);

    List<Patient> findAllByOrderByIdAsc();

    /** The patient profile linked to a PATIENT login account. */
    Optional<Patient> findByAccountUsername(String username);

    boolean existsByAccountId(Long userId);
}
