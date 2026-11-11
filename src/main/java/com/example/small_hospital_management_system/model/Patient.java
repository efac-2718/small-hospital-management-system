package com.example.small_hospital_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient details. Used directly as the request/response body of /api/patients,
 * so the validation rules live on the entity.
 */
@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE or OTHER")
    @Column(length = 10)
    private String gender;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone must contain 9 to 15 digits (optional leading +)")
    @Column(length = 20)
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
    @Column(length = 3)
    private String bloodGroup;

    /** Login account of the patient (set when a PATIENT registers). Staff-created patients may have none. */
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private AppUser account;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Username of the linked login account, shown to clients (read-only). */
    @JsonProperty(value = "username", access = JsonProperty.Access.READ_ONLY)
    public String getUsername() {
        return account == null ? null : account.getUsername();
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
