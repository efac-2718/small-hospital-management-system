package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.dto.AppointmentRequest;
import com.example.small_hospital_management_system.dto.AppointmentResponse;
import com.example.small_hospital_management_system.exception.ApiException;
import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import com.example.small_hospital_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Booking and listing appointments.
 * <ul>
 *   <li>Doctors only ever see (and update) their own appointments.</li>
 *   <li>Patients only ever see their own appointments and can only book for themselves.</li>
 *   <li>ADMIN / RECEPTIONIST / NURSE see all appointments.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public AppointmentResponse book(AppointmentRequest request, String username) {
        AppUser caller = getUser(username);
        AppUser doctor = userRepository.findById(request.doctorId())
                .filter(u -> u.getRole() == Role.DOCTOR && u.isEnabled())
                .orElseThrow(() -> ApiException.notFound("Doctor with id " + request.doctorId() + " not found"));

        Patient patient;
        if (caller.getRole() == Role.PATIENT) {
            patient = patientRepository.findByUsername(username)
                    .orElseThrow(() -> ApiException.notFound("No patient profile is linked to your account"));
            if (request.patientId() != null && !request.patientId().equals(patient.getId())) {
                throw ApiException.forbidden("Patients can only book appointments for themselves");
            }
        } else {
            if (request.patientId() == null) {
                throw ApiException.badRequest("patientId is required when staff book an appointment");
            }
            patient = patientRepository.findById(request.patientId())
                    .orElseThrow(() -> ApiException.notFound("Patient with id " + request.patientId() + " not found"));
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentTimeAndStatusNot(
                doctor.getId(), request.appointmentTime(), AppointmentStatus.CANCELLED)) {
            throw ApiException.conflict(doctor.getFullName() + " already has an appointment at "
                    + request.appointmentTime());
        }

        Appointment saved = appointmentRepository.save(Appointment.builder()
                .patientId(patient.getId())
                .patientName(patient.getFullName())
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .doctorSpecialization(doctor.getSpecialization())
                .appointmentTime(request.appointmentTime())
                .reason(request.reason())
                .status(AppointmentStatus.SCHEDULED)
                .build());
        return AppointmentResponse.from(saved);
    }

    /** Doctor: own list only. Patient: own list only. Other staff: everything. */
    public List<AppointmentResponse> listVisibleTo(String username) {
        AppUser caller = getUser(username);
        List<Appointment> appointments = switch (caller.getRole()) {
            case DOCTOR -> appointmentRepository.findByDoctorIdOrderByAppointmentTimeAsc(caller.getId());
            case PATIENT -> patientRepository.findByUsername(username)
                    .map(p -> appointmentRepository.findByPatientIdOrderByAppointmentTimeAsc(p.getId()))
                    .orElse(List.of());
            case ADMIN, NURSE, RECEPTIONIST -> appointmentRepository.findAllByOrderByAppointmentTimeAsc();
        };
        return appointments.stream().map(AppointmentResponse::from).toList();
    }

    public AppointmentResponse get(String id, String username) {
        Appointment appointment = find(id);
        AppUser caller = getUser(username);
        if ((caller.getRole() == Role.DOCTOR || caller.getRole() == Role.PATIENT) && !isOwnedBy(appointment, caller)) {
            throw ApiException.forbidden("You can only view your own appointments");
        }
        return AppointmentResponse.from(appointment);
    }

    public AppointmentResponse updateStatus(String id, AppointmentStatus newStatus, String username) {
        Appointment appointment = find(id);
        AppUser caller = getUser(username);

        switch (caller.getRole()) {
            case DOCTOR -> {
                if (!isOwnedBy(appointment, caller)) {
                    throw ApiException.forbidden("Doctors can only update their own appointments");
                }
            }
            case PATIENT -> {
                if (!isOwnedBy(appointment, caller)) {
                    throw ApiException.forbidden("You can only change your own appointments");
                }
                if (newStatus != AppointmentStatus.CANCELLED) {
                    throw ApiException.forbidden("Patients can only cancel their appointments");
                }
            }
            case NURSE -> throw ApiException.forbidden("Nurses cannot change appointment status");
            case ADMIN, RECEPTIONIST -> {
                // allowed for any appointment
            }
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && appointment.getStatus() != newStatus) {
            throw ApiException.conflict("Appointment " + id + " is already " + appointment.getStatus()
                    + " and cannot be changed");
        }
        appointment.setStatus(newStatus);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    /**
     * TODO Lab 7 (part 2): the OWNERSHIP rule. A role check alone is not enough: every doctor has the
     * DOCTOR role, but Dr. Perera must not complete Dr. Silva's appointment.
     *
     * Return true when:
     *   - caller is a DOCTOR  and  appointment.getDoctorId() equals caller.getId()
     *   - caller is a PATIENT and  the patient profile of caller.getUsername()
     *                              (patientRepository.findByUsername) has the appointment's patientId
     * Return false for everyone else.
     *
     * This kind of check is called object-level authorization. Missing it is the #1 API security bug
     * (OWASP API Security Top 10: "Broken Object Level Authorization").
     */
    boolean isOwnedBy(Appointment appointment, AppUser caller) {
        throw new UnsupportedOperationException("TODO Lab 7: isOwnedBy");
    }

    private Appointment find(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Appointment with id " + id + " not found"));
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User '" + username + "' not found"));
    }
}
