package com.example.small_hospital_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Links a patient and a doctor at a date and time ("appointments" collection).
 * <p>
 * MongoDB has no JOINs, so the document stores the ids of the patient and doctor (to look them up
 * and to check ownership) plus a copy of their names (so a list can be shown without extra queries).
 * Copying data like this is called <b>denormalisation</b>.
 */
@Document(collection = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    private String id;

    @Indexed
    private String patientId;
    private String patientName;

    /** Id of an AppUser whose role is DOCTOR. */
    @Indexed
    private String doctorId;
    private String doctorName;
    private String doctorSpecialization;

    private LocalDateTime appointmentTime;

    private String reason;

    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @CreatedDate
    private LocalDateTime createdAt;
}
