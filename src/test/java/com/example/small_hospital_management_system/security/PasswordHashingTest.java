package com.example.small_hospital_management_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lab 2 "learning test": experiments that show how BCrypt behaves.
 * A BCrypt hash looks like  $2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhW
 *                           |  |  |                     |
 *                           |  |  22-char random salt   31-char hash
 *                           |  cost (2^10 rounds)
 *                           algorithm version
 */
class PasswordHashingTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void theHashIsNotThePassword() {
        String hash = encoder.encode("PS/2022/123");

        assertThat(hash).isNotEqualTo("PS/2022/123");
        assertThat(hash).startsWith("$2a$10$").hasSize(60);
    }

    @Test
    void hashingTheSamePasswordTwiceGivesDifferentHashesBecauseOfTheRandomSalt() {
        String first = encoder.encode("admin123");
        String second = encoder.encode("admin123");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void matchesReadsTheSaltFromTheHashAndChecksThePassword() {
        String hash = encoder.encode("admin123");

        assertThat(encoder.matches("admin123", hash)).isTrue();
        assertThat(encoder.matches("Admin123", hash)).isFalse();
    }

    @Test
    void aHigherCostMakesHashingSlowerOnPurpose() {
        PasswordEncoder strong = new BCryptPasswordEncoder(12);

        assertThat(strong.encode("admin123")).startsWith("$2a$12$");
    }
}
