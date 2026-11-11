package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.exception.ApiException;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.MedicalRecordRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * LAB 1 - MongoDB documents and repositories.
 *
 * Implement the TODO methods using the repositories below. Open the repository interfaces first:
 * you never write a query - Spring Data builds it from the method NAME.
 *
 * Check your work:   ./mvnw test -Dtest=SecurityFlowIntegrationTests
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * TODO 1: return every patient sorted by name, or - if {@code name} has text - only the patients
     *         whose fullName contains it (ignoring case).
     * Hint:   StringUtils.hasText(name), and look at the two "find..." methods in PatientRepository.
     *         Which MongoDB query do you think findByFullNameContainingIgnoreCase... runs?
     */
    public List<Patient> findAll(String name) {
        throw new UnsupportedOperationException("TODO Lab 1: findAll");
    }

    /**
     * TODO 2: find the patient by id, or throw ApiException.notFound("Patient with id ... not found").
     * Hint:   findById returns an Optional - use orElseThrow(() -> ...).
     *         The GlobalExceptionHandler turns that exception into a 404 JSON response.
     */
    public Patient findById(String id) {
        throw new UnsupportedOperationException("TODO Lab 1: findById");
    }

    /** The profile of the logged-in PATIENT (already done - use it as an example). */
    public Patient findByUsername(String username) {
        return patientRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
    }

    public Patient create(Patient patient) {
        patient.setId(null);           // MongoDB generates a new ObjectId on insert
        patient.setUsername(null);     // clients may not link a patient to someone's account
        patient.setCreatedAt(null);    // filled in by @CreatedDate
        return patientRepository.save(patient);
    }

    public Patient update(String id, Patient changes) {
        Patient existing = findById(id);
        existing.setFullName(changes.getFullName());
        existing.setDateOfBirth(changes.getDateOfBirth());
        existing.setGender(changes.getGender());
        existing.setPhone(changes.getPhone());
        existing.setEmail(changes.getEmail());
        existing.setAddress(changes.getAddress());
        existing.setBloodGroup(changes.getBloodGroup());
        return patientRepository.save(existing);   // save() with an existing id = update
    }

    /**
     * TODO 3: delete the patient, BUT if they still have appointments or medical records throw
     *         ApiException.conflict(...) (HTTP 409) instead.
     * Why:    MongoDB has no foreign keys, so nothing stops you deleting a patient that other
     *         documents still point to. The application has to enforce that rule itself.
     * Hint:   findById(id) first (gives the 404), then existsByPatientId(...) on both repositories.
     */
    public void delete(String id) {
        throw new UnsupportedOperationException("TODO Lab 1: delete");
    }
}
