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
 * A login account (staff member or patient), stored in the "users" collection.
 * The password field only ever holds a BCrypt hash.
 */
@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    /** MongoDB ObjectId, e.g. "6731f0c2a1b2c3d4e5f60718". */
    @Id
    private String id;

    /** A unique index stops two accounts having the same username. */
    @Indexed(unique = true)
    private String username;

    /** BCrypt hash, never the plain-text password. */
    private String password;

    private String fullName;

    private Role role;

    /** Only used for doctors, e.g. "Cardiology". */
    private String specialization;

    @Builder.Default
    private boolean enabled = true;

    /** Filled in automatically on the first save (see MongoConfig: @EnableMongoAuditing). */
    @CreatedDate
    private LocalDateTime createdAt;
}
