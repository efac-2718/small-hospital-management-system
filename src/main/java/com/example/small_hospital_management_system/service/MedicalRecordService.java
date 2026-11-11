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

import java.util.List;

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

    public MedicalRecordResponse create(MedicalRecordRequest request, String doctorUsername) {
        AppUser doctor = getUser(doctorUsername);
        if (doctor.getRole() != Role.DOCTOR) {
            throw ApiException.forbidden("Only doctors can write medical records");
        }
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> ApiException.notFound("Patient with id " + request.patientId() + " not found"));

        if (request.appointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.appointmentId())
                    .orElseThrow(() -> ApiException.notFound(
                            "Appointment with id " + request.appointmentId() + " not found"));
            if (!doctor.getId().equals(appointment.getDoctorId())) {
                throw ApiException.forbidden("You can only write records for your own appointments");
            }
            if (!patient.getId().equals(appointment.getPatientId())) {
                throw ApiException.badRequest("Appointment " + appointment.getId()
                        + " does not belong to patient " + patient.getId());
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                throw ApiException.conflict("Cannot write a record for a cancelled appointment");
            }
        }

        MedicalRecord saved = medicalRecordRepository.save(MedicalRecord.builder()
                .patientId(patient.getId())
                .patientName(patient.getFullName())
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .appointmentId(request.appointmentId())
                .diagnosis(request.diagnosis())
                .prescription(request.prescription())
                .notes(request.notes())
                .build());
        return MedicalRecordResponse.from(saved);
    }

    public List<MedicalRecordResponse> listForPatient(String patientId, String username) {
        AppUser caller = getUser(username);
        if (caller.getRole() == Role.PATIENT) {
            String ownId = patientRepository.findByUsername(username).map(Patient::getId).orElse(null);
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
    public List<MedicalRecordResponse> listMine(String username) {
        Patient patient = patientRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
        return medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream()
                .map(MedicalRecordResponse::from)
                .toList();
    }

    public MedicalRecordResponse get(String id, String username) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Medical record with id " + id + " not found"));
        AppUser caller = getUser(username);
        if (caller.getRole() == Role.PATIENT) {
            boolean own = patientRepository.findByUsername(username)
                    .map(p -> p.getId().equals(record.getPatientId()))
                    .orElse(false);
            if (!own) {
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
