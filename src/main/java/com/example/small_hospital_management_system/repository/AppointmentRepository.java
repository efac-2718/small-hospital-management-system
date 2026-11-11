package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** All appointments (staff view). */
    List<Appointment> findAllByOrderByAppointmentTimeAsc();

    /** Appointments of one doctor. */
    List<Appointment> findByDoctorUsernameOrderByAppointmentTimeAsc(String doctorUsername);

    /** Appointments of one patient. */
    List<Appointment> findByPatientIdOrderByAppointmentTimeAsc(Long patientId);

    /** Does the patient have any appointment? (blocks deleting the patient) */
    boolean existsByPatientId(Long patientId);

    boolean existsByDoctorId(Long doctorId);

    /** Is the doctor already booked at this exact time? */
    boolean existsByDoctorIdAndAppointmentTimeAndStatusNot(Long doctorId, LocalDateTime time, AppointmentStatus status);

    long countByStatus(AppointmentStatus status);
}
