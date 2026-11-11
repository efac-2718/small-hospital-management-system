package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.dto.MedicalRecordRequest;
import com.example.small_hospital_management_system.dto.MedicalRecordResponse;
import com.example.small_hospital_management_system.exception.ApiException;
import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import com.example.small_hospital_management_system.model.MedicalRecord;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.MedicalRecordRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import com.example.small_hospital_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Medical records. The author is always the doctor identified by the JWT, so a doctor cannot write
 * a record in another doctor's name. Patients can only read their own records.
 */
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request, String doctorUsername) {
        AppUser doctor = getUser(doctorUsername);
        if (doctor.getRole() != Role.DOCTOR) {
            throw ApiException.forbidden("Only doctors can write medical records");
        }
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> ApiException.notFound("Patient with id " + request.patientId() + " not found"));

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> ApiException.notFound(
                            "Appointment with id " + request.appointmentId() + " not found"));
            if (!Objects.equals(appointment.getDoctor().getId(), doctor.getId())) {
                throw ApiException.forbidden("You can only write records for your own appointments");
            }
            if (!Objects.equals(appointment.getPatient().getId(), patient.getId())) {
                throw ApiException.badRequest("Appointment " + appointment.getId()
                        + " does not belong to patient " + patient.getId());
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                throw ApiException.conflict("Cannot write a record for a cancelled appointment");
            }
        }

        MedicalRecord saved = medicalRecordRepository.save(MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .diagnosis(request.diagnosis())
                .prescription(request.prescription())
                .notes(request.notes())
                .build());
        return MedicalRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listForPatient(Long patientId, String username) {
        AppUser caller = getUser(username);
        if (caller.getRole() == Role.PATIENT) {
            Long ownId = patientRepository.findByAccountUsername(username).map(Patient::getId).orElse(null);
            if (!patientId.equals(ownId)) {
                throw ApiException.forbidden("You can only view your own medical records");
            }
        } else if (!patientRepository.existsById(patientId)) {
            throw ApiException.notFound("Patient with id " + patientId + " not found");
        }
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(MedicalRecordResponse::from)
                .toList();
    }

    /** Records of the logged-in PATIENT. */
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> listMine(String username) {
        Patient patient = patientRepository.findByAccountUsername(username)
                .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .map(MedicalRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse get(Long id, String username) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Medical record with id " + id + " not found"));
        AppUser caller = getUser(username);
        if (caller.getRole() == Role.PATIENT) {
            AppUser owner = record.getPatient().getAccount();
            if (owner == null || !Objects.equals(owner.getId(), caller.getId())) {
                throw ApiException.forbidden("You can only view your own medical records");
            }
        }
        return MedicalRecordResponse.from(record);
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User '" + username + "' not found"));
    }
}
