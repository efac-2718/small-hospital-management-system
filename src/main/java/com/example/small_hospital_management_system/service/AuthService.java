package com.example.small_hospital_management_system.service;

import com.example.small_hospital_management_system.dto.AuthResponse;
import com.example.small_hospital_management_system.dto.LoginRequest;
import com.example.small_hospital_management_system.dto.RegisterRequest;
import com.example.small_hospital_management_system.dto.UserResponse;
import com.example.small_hospital_management_system.exception.ApiException;
import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.MedicalRecordRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import com.example.small_hospital_management_system.repository.UserRepository;
import com.example.small_hospital_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Login, register, "me", and listing users and doctors. */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** Checks the username + BCrypt password and returns a signed JWT. Wrong credentials give 401. */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        AppUser user = getUser(request.username());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, TOKEN_TYPE, jwtService.getExpirationMs(),
                user.getUsername(), user.getFullName(), user.getRole());
    }

    /**
     * Anyone may self-register as a PATIENT (the default role).
     * Staff accounts (ADMIN, DOCTOR, NURSE, RECEPTIONIST) need a valid token that belongs to an ADMIN.
     */
    public UserResponse register(RegisterRequest request) {
        Role role = request.role() == null ? Role.PATIENT : request.role();
        if (role.isStaff()) {
            requireAdminCaller(role);
        }
        return UserResponse.from(createUser(request, role));
    }

    /** ADMIN adds a new doctor profile (POST /api/admin/doctors). The role is always DOCTOR. */
    public UserResponse createDoctor(RegisterRequest request) {
        return UserResponse.from(createUser(request, Role.DOCTOR));
    }

    public UserResponse me(String username) {
        return UserResponse.from(getUser(username));
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public List<UserResponse> listDoctors() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.DOCTOR).stream()
                .filter(AppUser::isEnabled)
                .map(UserResponse::from)
                .toList();
    }

    /** ADMIN deletes an account. Blocked (409) if it would orphan appointments or medical records. */
    public void deleteUser(String id, String currentUsername) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User with id " + id + " not found"));
        if (user.getUsername().equals(currentUsername)) {
            throw ApiException.conflict("You cannot delete your own account");
        }
        if (user.getRole() == Role.DOCTOR
                && (appointmentRepository.existsByDoctorId(id) || medicalRecordRepository.existsByDoctorId(id))) {
            throw ApiException.conflict("Doctor '" + user.getUsername()
                    + "' has appointments or medical records and cannot be deleted");
        }
        if (user.getRole() == Role.PATIENT) {
            patientRepository.findByUsername(user.getUsername()).ifPresent(patient -> {
                if (appointmentRepository.existsByPatientId(patient.getId())
                        || medicalRecordRepository.existsByPatientId(patient.getId())) {
                    throw ApiException.conflict("Patient '" + user.getUsername()
                            + "' has appointments or medical records and cannot be deleted");
                }
                patientRepository.delete(patient);
            });
        }
        userRepository.delete(user);
    }

    private AppUser createUser(RegisterRequest request, Role role) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Username '" + username + "' is already taken");
        }
        AppUser user = userRepository.save(AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(role)
                .specialization(role == Role.DOCTOR ? request.specialization() : null)
                .enabled(true)
                .build());

        // Every PATIENT account gets a patient profile so it can book appointments.
        if (role == Role.PATIENT) {
            patientRepository.save(Patient.builder()
                    .fullName(user.getFullName())
                    .username(user.getUsername())
                    .build());
        }
        return user;
    }

    private void requireAdminCaller(Role requestedRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        if (!loggedIn) {
            throw ApiException.unauthorized("Creating a " + requestedRole
                    + " account requires an ADMIN token (Authorization: Bearer <token>)");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Role.ADMIN.authority().equals(a.getAuthority()));
        if (!isAdmin) {
            throw ApiException.forbidden("Only an ADMIN can create " + requestedRole + " accounts");
        }
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("User '" + username + "' not found"));
    }
}
