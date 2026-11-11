package com.example.small_hospital_management_system.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient details, stored in the "patients" collection. Used directly as the request/response
 * body of /api/patients, so the validation rules live here.
 */
@Document(collection = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    @Indexed
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE or OTHER")
    private String gender;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone must contain 9 to 15 digits (optional leading +)")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100)
    private String email;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    /**
     * Username of the PATIENT login account linked to this profile (set when a patient self-registers).
     * Staff-created patients have none. MongoDB has no foreign keys, so we store the link ourselves.
     * "sparse" lets many patients have no username while usernames that exist stay unique.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Indexed(unique = true, sparse = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedDate
    private LocalDateTime createdAt;
}
