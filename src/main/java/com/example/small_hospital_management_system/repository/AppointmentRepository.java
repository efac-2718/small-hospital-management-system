package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    /** All appointments (staff view). */
    List<Appointment> findAllByOrderByAppointmentTimeAsc();

    /** Appointments of one doctor. */
    List<Appointment> findByDoctorIdOrderByAppointmentTimeAsc(String doctorId);

    /** Appointments of one patient. */
    List<Appointment> findByPatientIdOrderByAppointmentTimeAsc(String patientId);

    /** Does the patient have any appointment? (blocks deleting the patient) */
    boolean existsByPatientId(String patientId);

    boolean existsByDoctorId(String doctorId);

    /** Is the doctor already booked at this exact time? */
    boolean existsByDoctorIdAndAppointmentTimeAndStatusNot(String doctorId, LocalDateTime time,
                                                           AppointmentStatus status);

    long countByStatus(AppointmentStatus status);
}
