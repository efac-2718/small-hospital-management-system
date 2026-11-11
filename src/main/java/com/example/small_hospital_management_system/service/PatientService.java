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

/** Patient CRUD. A patient who has appointments or medical records cannot be deleted. */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public List<Patient> findAll(String name) {
        if (StringUtils.hasText(name)) {
            return patientRepository.findByFullNameContainingIgnoreCaseOrderByFullNameAsc(name.trim());
        }
        return patientRepository.findAllByOrderByFullNameAsc();
    }

    public Patient findById(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Patient with id " + id + " not found"));
    }

    /** The profile of the logged-in PATIENT. */
    public Patient findByUsername(String username) {
        return patientRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
    }

    public Patient create(Patient patient) {
        patient.setId(null);
        patient.setUsername(null);
        patient.setCreatedAt(null);
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
        return patientRepository.save(existing);
    }

    public void delete(String id) {
        Patient patient = findById(id);
        if (appointmentRepository.existsByPatientId(id) || medicalRecordRepository.existsByPatientId(id)) {
            throw ApiException.conflict("Patient " + id
                    + " has appointments or medical records and cannot be deleted");
        }
        patientRepository.delete(patient);
    }
}
