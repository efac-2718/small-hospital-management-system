package com.example.small_hospital_management_system;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests over real HTTP (like Postman): authentication, RBAC and the
 * Task 2 JWT-tampering scenario.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityFlowIntegrationTests {

    private static final AtomicInteger COUNTER = new AtomicInteger(100);
    private static final AtomicInteger SLOT = new AtomicInteger(1);

    @LocalServerPort
    private int port;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = new JsonMapper();

    // ------------------------------------------------------------------ authentication

    @Test
    void loginReturnsHs384TokenWithSubRoleIatExp() {
        Resp res = post("/api/auth/login", null, Map.of("username", "admin", "password", "admin123"));

        assertThat(res.status).isEqualTo(200);
        assertThat(res.body.path("tokenType").asString()).isEqualTo("Bearer");
        assertThat(res.body.path("expiresInMs").asLong()).isEqualTo(3_600_000L);
        assertThat(res.body.path("role").asString()).isEqualTo("ADMIN");

        String[] parts = res.body.path("token").asString().split("\\.");
        assertThat(parts).hasSize(3);
        JsonNode header = json.readTree(decode(parts[0]));
        JsonNode payload = json.readTree(decode(parts[1]));
        assertThat(header.path("alg").asString()).isEqualTo("HS384");
        assertThat(payload.path("sub").asString()).isEqualTo("admin");
        assertThat(payload.path("role").asString()).isEqualTo("ADMIN");
        assertThat(payload.has("iat")).isTrue();
        assertThat(payload.has("exp")).isTrue();
    }

    @Test
    void loginWithWrongPasswordIs401() {
        Resp res = post("/api/auth/login", null, Map.of("username", "admin", "password", "wrong-password"));

        assertThat(res.status).isEqualTo(401);
        assertThat(res.body.path("message").asString()).isEqualTo("Invalid username or password");
    }

    @Test
    void patientCanSelfRegisterAndPasswordIsNeverReturned() {
        String username = newStudentNumber();
        Resp res = registerPatient(username);

        assertThat(res.status).isEqualTo(201);
        assertThat(res.body.path("username").asString()).isEqualTo(username);
        assertThat(res.body.path("role").asString()).isEqualTo("PATIENT");
        assertThat(res.body.has("password")).isFalse();

        Resp duplicate = registerPatient(username);
        assertThat(duplicate.status).isEqualTo(409);
    }

    @Test
    void staffAccountsCanOnlyBeCreatedByAnAdmin() {
        String patientToken = registerAndLoginPatient();
        String adminToken = login("admin", "admin123");

        assertThat(post("/api/auth/register", null, nurseBody(newStudentNumber())).status).isEqualTo(401);
        assertThat(post("/api/auth/register", patientToken, nurseBody(newStudentNumber())).status).isEqualTo(403);

        Resp created = post("/api/auth/register", adminToken, nurseBody(newStudentNumber()));
        assertThat(created.status).isEqualTo(201);
        assertThat(created.body.path("role").asString()).isEqualTo("NURSE");
    }

    // ------------------------------------------------------------------ Task 2: tampered tokens

    @Test
    void task2_tamperedRoleClaimIsRejectedWith401() {
        String patientToken = registerAndLoginPatient();
        String tampered = changeRoleKeepingSignature(patientToken, "PATIENT", "ADMIN");

        // Step 4: try to register a nurse with the modified token
        Resp nurse = post("/api/auth/register", tampered, nurseBody(newStudentNumber()));
        assertThat(nurse.status).isEqualTo(401);
        assertThat(nurse.body.path("message").asString()).contains("Invalid JWT signature");

        // Step 5: the same token cannot read patient records either
        Resp patients = get("/api/patients", tampered);
        assertThat(patients.status).isEqualTo(401);

        // The genuine patient token is still only a PATIENT
        assertThat(get("/api/patients", patientToken).status).isEqualTo(403);
    }

    @Test
    void tokenResignedWithAnotherSecretIsRejected() {
        // What a JWT encoder such as jwt.io produces: a valid-looking token signed with the attacker's key
        String forged = Jwts.builder()
                .setSubject("admin")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor("a-string-secret-at-least-384-bits-long-for-hs384-ok"
                        .getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS384)
                .compact();

        assertThat(get("/api/admin/patients", forged).status).isEqualTo(401);
    }

    @Test
    void unsignedAlgNoneTokenIsRejected() {
        String header = encode("{\"alg\":\"none\"}");
        String payload = encode("{\"sub\":\"admin\",\"role\":\"ADMIN\",\"exp\":"
                + (System.currentTimeMillis() / 1000 + 3600) + "}");

        assertThat(get("/api/admin/patients", header + "." + payload + ".").status).isEqualTo(401);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService expiredIssuer = new JwtService(jwtSecret, -60_000);
        String expired = expiredIssuer.generateToken(
                AppUser.builder().username("admin").role(Role.ADMIN).build());

        Resp res = get("/api/admin/patients", expired);
        assertThat(res.status).isEqualTo(401);
        assertThat(res.body.path("message").asString()).contains("expired");
    }

    @Test
    void protectedEndpointsNeedAToken() {
        Resp res = get("/api/patients", null);

        assertThat(res.status).isEqualTo(401);
        assertThat(res.headers.firstValue("WWW-Authenticate")).contains("Bearer");
    }

    // ------------------------------------------------------------------ RBAC

    @Test
    void patientListIsForStaffAndAdminPathIsAdminOnly() {
        String admin = login("admin", "admin123");
        String doctor = login("dr.silva", "doctor123");
        String patient = registerAndLoginPatient();

        Resp all = get("/api/admin/patients", admin);
        assertThat(all.status).isEqualTo(200);
        // The linked login account is exposed only as its username - never the account or its BCrypt hash
        assertThat(all.body.toString()).doesNotContain("password", "account", "$2a$").contains("\"username\":\"patient\"");
        assertThat(get("/api/admin/patients", doctor).status).isEqualTo(403);
        assertThat(get("/api/admin/patients", patient).status).isEqualTo(403);

        assertThat(get("/api/patients", doctor).status).isEqualTo(200);
        assertThat(get("/api/patients", patient).status).isEqualTo(403);

        Resp search = get("/api/patients?name=amaya", admin);
        assertThat(search.status).isEqualTo(200);
        assertThat(search.body.size()).isEqualTo(1);
        assertThat(search.body.get(0).path("fullName").asString()).isEqualTo("Amaya Wickramasinghe");
    }

    @Test
    void adminCanAddDoctorAndViewStatsButOthersCannot() {
        String admin = login("admin", "admin123");
        String patient = registerAndLoginPatient();
        Map<String, Object> doctor = Map.of("username", newStudentNumber(), "password", "doctor123",
                "fullName", "Dr. Test Doctor", "specialization", "Neurology", "role", "PATIENT");

        assertThat(post("/api/admin/doctors", patient, doctor).status).isEqualTo(403);

        Resp created = post("/api/admin/doctors", admin, doctor);
        assertThat(created.status).isEqualTo(201);
        assertThat(created.body.path("role").asString()).isEqualTo("DOCTOR");
        assertThat(created.body.path("specialization").asString()).isEqualTo("Neurology");

        Resp stats = get("/api/admin/stats", admin);
        assertThat(stats.status).isEqualTo(200);
        assertThat(stats.body.path("totalDoctors").asLong()).isGreaterThanOrEqualTo(3);
        assertThat(get("/api/admin/stats", patient).status).isEqualTo(403);
    }

    @Test
    void appointmentAndMedicalRecordFlowEnforcesOwnership() {
        String patient = registerAndLoginPatient();
        String otherPatient = registerAndLoginPatient();
        String drSilva = login("dr.silva", "doctor123");
        String drPerera = login("dr.perera", "doctor123");
        String nurse = login("nurse.fernando", "nurse123");
        String admin = login("admin", "admin123");

        long doctorId = doctorId(patient, "dr.silva");
        long patientId = get("/api/patients/me", patient).body.path("id").asLong();
        long otherPatientId = get("/api/patients/me", otherPatient).body.path("id").asLong();
        String time = nextFreeSlot();

        // Patient books for themselves
        Resp booked = post("/api/patients/appointments", patient,
                Map.of("doctorId", doctorId, "appointmentTime", time, "reason", "Headache"));
        assertThat(booked.status).isEqualTo(201);
        assertThat(booked.body.path("patientId").asLong()).isEqualTo(patientId);
        assertThat(booked.body.path("status").asString()).isEqualTo("SCHEDULED");
        long appointmentId = booked.body.path("id").asLong();

        // Same doctor + same time -> 409; past date -> 400; booking for someone else -> 403
        assertThat(post("/api/patients/appointments", otherPatient,
                Map.of("doctorId", doctorId, "appointmentTime", time)).status).isEqualTo(409);
        assertThat(post("/api/patients/appointments", patient,
                Map.of("doctorId", doctorId, "appointmentTime", "2020-01-01T10:00:00")).status).isEqualTo(400);
        assertThat(post("/api/patients/appointments", patient,
                Map.of("doctorId", doctorId, "patientId", otherPatientId, "appointmentTime", nextFreeSlot()))
                .status).isEqualTo(403);

        // Doctors only see their own appointments; patients cannot use the doctor endpoint
        assertThat(ids(get("/api/doctors/appointments", drSilva))).contains(appointmentId);
        assertThat(ids(get("/api/doctors/appointments", drPerera))).doesNotContain(appointmentId);
        assertThat(ids(get("/api/doctors/appointments", admin))).contains(appointmentId);
        assertThat(get("/api/doctors/appointments", patient).status).isEqualTo(403);
        assertThat(ids(get("/api/patients/appointments", otherPatient))).doesNotContain(appointmentId);
        assertThat(get("/api/appointments/" + appointmentId, otherPatient).status).isEqualTo(403);

        // Only the appointment's doctor writes the record; the author comes from the token
        Map<String, Object> record = Map.of("patientId", patientId, "appointmentId", appointmentId,
                "diagnosis", "Migraine", "prescription", "Paracetamol 500mg");
        assertThat(post("/api/records", nurse, record).status).isEqualTo(403);
        assertThat(post("/api/records", drPerera, record).status).isEqualTo(403);
        Resp written = post("/api/records", drSilva, record);
        assertThat(written.status).isEqualTo(201);
        assertThat(written.body.path("doctorName").asString()).isEqualTo("Dr. Nimal Silva");

        // Patients read only their own records
        assertThat(get("/api/patients/records", patient).body.size()).isEqualTo(1);
        assertThat(get("/api/records/patient/" + patientId, otherPatient).status).isEqualTo(403);
        assertThat(get("/api/records/patient/" + patientId, nurse).status).isEqualTo(200);

        // Status changes: other doctor 403, own doctor 200, finished appointment cannot change (409)
        assertThat(patch("/api/appointments/" + appointmentId + "/status", drPerera,
                Map.of("status", "COMPLETED")).status).isEqualTo(403);
        assertThat(patch("/api/appointments/" + appointmentId + "/status", drSilva,
                Map.of("status", "COMPLETED")).status).isEqualTo(200);
        assertThat(patch("/api/appointments/" + appointmentId + "/status", patient,
                Map.of("status", "CANCELLED")).status).isEqualTo(409);

        // A patient with data cannot be deleted
        assertThat(delete("/api/patients/" + patientId, admin).status).isEqualTo(409);
    }

    @Test
    void staffCanManagePatientsWithValidation() {
        String reception = login("reception", "reception123");
        String admin = login("admin", "admin123");
        String doctor = login("dr.silva", "doctor123");

        Resp invalid = post("/api/patients", reception,
                Map.of("fullName", "", "email", "not-an-email", "dateOfBirth", "2999-01-01"));
        assertThat(invalid.status).isEqualTo(400);
        assertThat(invalid.body.path("fieldErrors").has("fullName")).isTrue();
        assertThat(invalid.body.path("fieldErrors").has("email")).isTrue();
        assertThat(invalid.body.path("fieldErrors").has("dateOfBirth")).isTrue();

        Map<String, Object> valid = Map.of("fullName", "Kasun Perera", "dateOfBirth", "1990-05-12",
                "gender", "MALE", "phone", "0771112223", "email", "kasun@example.com", "bloodGroup", "AB+");
        assertThat(post("/api/patients", doctor, valid).status).isEqualTo(403);
        Resp created = post("/api/patients", reception, valid);
        assertThat(created.status).isEqualTo(201);
        long id = created.body.path("id").asLong();

        assertThat(delete("/api/patients/" + id, reception).status).isEqualTo(403);
        assertThat(delete("/api/patients/" + id, admin).status).isEqualTo(204);
        assertThat(get("/api/patients/" + id, admin).status).isEqualTo(404);
    }

    // ------------------------------------------------------------------ helpers

    private record Resp(int status, JsonNode body, java.net.http.HttpHeaders headers) {
    }

    private String newStudentNumber() {
        return "PS/2022/" + COUNTER.getAndIncrement();
    }

    private String nextFreeSlot() {
        return LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.DAYS)
                .plusMinutes(15L * SLOT.getAndIncrement()).toString();
    }

    private Resp registerPatient(String username) {
        return post("/api/auth/register", null,
                Map.of("username", username, "password", username, "fullName", "Test User", "role", "PATIENT"));
    }

    private String registerAndLoginPatient() {
        String username = newStudentNumber();
        assertThat(registerPatient(username).status).isEqualTo(201);
        return login(username, username);
    }

    private Map<String, Object> nurseBody(String username) {
        return Map.of("name", "Test", "username", username, "password", username, "role", "NURSE");
    }

    private String login(String username, String password) {
        Resp res = post("/api/auth/login", null, Map.of("username", username, "password", password));
        assertThat(res.status).as("login " + username).isEqualTo(200);
        return res.body.path("token").asString();
    }

    private long doctorId(String token, String username) {
        for (JsonNode doctor : get("/api/doctors", token).body) {
            if (doctor.path("username").asString().equals(username)) {
                return doctor.path("id").asLong();
            }
        }
        throw new AssertionError("Doctor not found: " + username);
    }

    private java.util.List<Long> ids(Resp res) {
        assertThat(res.status).isEqualTo(200);
        java.util.List<Long> ids = new java.util.ArrayList<>();
        res.body.forEach(node -> ids.add(node.path("id").asLong()));
        return ids;
    }

    /** Exactly what a student does on jwt.io: edit the payload, keep the header and the old signature. */
    private String changeRoleKeepingSignature(String token, String from, String to) {
        String[] parts = token.split("\\.");
        String payload = decode(parts[1]).replace("\"role\":\"" + from + "\"", "\"role\":\"" + to + "\"");
        assertThat(payload).contains("\"role\":\"" + to + "\"");
        return parts[0] + "." + encode(payload) + "." + parts[2];
    }

    private static String decode(String base64Url) {
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private Resp get(String path, String token) {
        return send("GET", path, token, null);
    }

    private Resp post(String path, String token, Object body) {
        return send("POST", path, token, body);
    }

    private Resp patch(String path, String token, Object body) {
        return send("PATCH", path, token, body);
    }

    private Resp delete(String path, String token) {
        return send("DELETE", path, token, null);
    }

    private Resp send(String method, String path, String token, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .header("Accept", "application/json");
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            if (body != null) {
                builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode parsed = response.body() == null || response.body().isBlank()
                    ? json.missingNode()
                    : json.readTree(response.body());
            return new Resp(response.statusCode(), parsed, response.headers());
        } catch (Exception e) {
            throw new IllegalStateException(method + " " + path + " failed", e);
        }
    }
}
