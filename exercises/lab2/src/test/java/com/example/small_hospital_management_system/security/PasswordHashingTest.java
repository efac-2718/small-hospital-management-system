package com.example.small_hospital_management_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * LAB 2 - a "learning test": you write the assertions to discover how BCrypt behaves.
 * Replace each fail(...) with real assertions, then run:
 *
 *     ./mvnw test -Dtest=PasswordHashingTest
 *
 * Tip: add System.out.println(hash) to SEE the hashes in the test output.
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
        // TODO: assert the hash is different from the password,
        //       starts with "$2a$10$" and is 60 characters long
        fail("TODO Lab 2");
    }

    @Test
    void hashingTheSamePasswordTwiceGivesDifferentHashesBecauseOfTheRandomSalt() {
        // TODO: encode "admin123" twice and assert the two hashes are NOT equal.
        // Question: if the hashes differ, how can login ever work? (next test)
        fail("TODO Lab 2");
    }

    @Test
    void matchesReadsTheSaltFromTheHashAndChecksThePassword() {
        String hash = encoder.encode("admin123");
        // TODO: assert encoder.matches("admin123", hash) is true
        //       and encoder.matches("Admin123", hash) is false
        fail("TODO Lab 2");
    }

    @Test
    void aHigherCostMakesHashingSlowerOnPurpose() {
        // TODO: create new BCryptPasswordEncoder(12) and assert its hashes start with "$2a$12$".
        // Extra: time encode() with cost 10, 12 and 14 (System.nanoTime()). Why is "slow" GOOD here?
        fail("TODO Lab 2");
    }
}
