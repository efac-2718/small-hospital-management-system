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

/** Diagnosis and prescription written by a doctor for a patient ("medical_records" collection). */
@Document(collection = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    private String id;

    @Indexed
    private String patientId;
    private String patientName;

    /** The doctor who wrote the record (taken from the JWT, never from the request body). */
    @Indexed
    private String doctorId;
    private String doctorName;

    /** Optional: the appointment this record was written for. */
    private String appointmentId;

    private String diagnosis;

    private String prescription;

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;
}
