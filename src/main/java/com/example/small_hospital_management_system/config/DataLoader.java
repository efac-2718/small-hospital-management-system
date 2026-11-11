package com.example.small_hospital_management_system.config;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Appointment;
import com.example.small_hospital_management_system.model.AppointmentStatus;
import com.example.small_hospital_management_system.model.Patient;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.AppointmentRepository;
import com.example.small_hospital_management_system.repository.PatientRepository;
import com.example.small_hospital_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Creates the seeded accounts and sample patients on the first start (when the users table is empty).
 * Disable with app.seed.enabled=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Database already has users - skipping seed data");
            return;
        }

        user("admin", "admin123", "System Administrator", Role.ADMIN, null);
        AppUser drSilva = user("dr.silva", "doctor123", "Dr. Nimal Silva", Role.DOCTOR, "Cardiology");
        user("dr.perera", "doctor123", "Dr. Kamala Perera", Role.DOCTOR, "Paediatrics");
        user("nurse.fernando", "nurse123", "Nadeesha Fernando", Role.NURSE, null);
        user("reception", "reception123", "Ruwani Jayawardena", Role.RECEPTIONIST, null);
        AppUser patientAccount = user("patient", "patient123", "Sunil Jayasinghe", Role.PATIENT, null);

        Patient sunil = patientRepository.save(Patient.builder()
                .fullName("Sunil Jayasinghe")
                .dateOfBirth(LocalDate.of(1985, 3, 14))
                .gender("MALE")
                .phone("0771234567")
                .email("sunil@example.com")
                .address("12 Temple Road, Kelaniya")
                .bloodGroup("O+")
                .account(patientAccount)
                .build());
        patientRepository.save(Patient.builder()
                .fullName("Amaya Wickramasinghe")
                .dateOfBirth(LocalDate.of(1992, 7, 2))
                .gender("FEMALE")
                .phone("0719876543")
                .email("amaya@example.com")
                .address("45 Kandy Road, Kiribathgoda")
                .bloodGroup("A+")
                .build());
        patientRepository.save(Patient.builder()
                .fullName("Ruwan Bandara")
                .dateOfBirth(LocalDate.of(1978, 11, 23))
                .gender("MALE")
                .phone("0754567890")
                .address("8 Station Road, Gampaha")
                .bloodGroup("B-")
                .build());

        appointmentRepository.save(Appointment.builder()
                .patient(sunil)
                .doctor(drSilva)
                .appointmentTime(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0)))
                .reason("Chest pain follow-up")
                .status(AppointmentStatus.SCHEDULED)
                .build());

        log.info("Seeded accounts (username / password): admin/admin123, dr.silva/doctor123, "
                + "dr.perera/doctor123, nurse.fernando/nurse123, reception/reception123, patient/patient123");
    }

    private AppUser user(String username, String password, String fullName, Role role, String specialization) {
        return userRepository.save(AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .specialization(specialization)
                .enabled(true)
                .build());
    }
}
