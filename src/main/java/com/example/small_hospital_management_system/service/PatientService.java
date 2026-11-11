package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.exception.ApiException;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.MedicalRecordRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** Patient CRUD. A patient who has appointments or medical records cannot be deleted. */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public List<Patient> findAll(String name) {
        if (StringUtils.hasText(name)) {
            return patientRepository.findByFullNameContainingIgnoreCaseOrderByFullNameAsc(name.trim());
        }
        return patientRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Patient with id " + id + " not found"));
    }

    /** The profile of the logged-in PATIENT. */
    @Transactional(readOnly = true)
    public Patient findByUsername(String username) {
        return patientRepository.findByAccountUsername(username)
                .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
    }

    @Transactional
    public Patient create(Patient patient) {
        patient.setId(null);
        patient.setAccount(null);
        patient.setCreatedAt(null);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient update(Long id, Patient changes) {
        Patient existing = findById(id);
        existing.setFullName(changes.getFullName());
        existing.setDateOfBirth(changes.getDateOfBirth());
        existing.setGender(changes.getGender());
        existing.setPhone(changes.getPhone());
        existing.setEmail(changes.getEmail());
        existing.setAddress(changes.getAddress());
        existing.setBloodGroup(changes.getBloodGroup());
        return patientRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Patient patient = findById(id);
        if (appointmentRepository.existsByPatientId(id) || medicalRecordRepository.existsByPatientId(id)) {
            throw ApiException.conflict("Patient " + id
                    + " has appointments or medical records and cannot be deleted");
        }
        patientRepository.delete(patient);
    }
}
