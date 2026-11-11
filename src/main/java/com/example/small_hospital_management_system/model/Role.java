package com.example.small_hospital_management_system.model;

/**
 * System roles used for Role-Based Access Control (RBAC).
 * Spring Security sees each one as an authority with the "ROLE_" prefix (e.g. ROLE_ADMIN).
 */
public enum Role {
    ADMIN,
    DOCTOR,
    NURSE,
    RECEPTIONIST,
    PATIENT;

    /** Staff roles can only be given to a new account by an ADMIN. */
    public boolean isStaff() {
        return this != PATIENT;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
