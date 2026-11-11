package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Lab 4 checker: creating and validating JWTs. Plain unit test - no Spring, no database. */
class JwtServiceTest {

    /** 48 random bytes, Base64 encoded (the same format as app.jwt.secret). */
    static final String SECRET = "Befa/RHbMrr0U+W0nXCBAflEAJZLPy2tXIs6PSfgksNPMetLtx6MJLpybvxBHHwK";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    private final AppUser patient = AppUser.builder().username("PS/2022/123").role(Role.PATIENT).build();

    @Test
    void tokenHasThreePartsAndIsSignedWithHs384() {
        String token = jwtService.generateToken(patient);

        String[] parts = token.split("\\.");
        assertThat(parts).as("header.payload.signature").hasSize(3);
        assertThat(decode(parts[0])).isEqualTo("{\"alg\":\"HS384\"}");
    }

    @Test
    void payloadContainsSubRoleIatAndExp() {
        String token = jwtService.generateToken(patient);

        String payload = decode(token.split("\\.")[1]);
        assertThat(payload).startsWith("{\"sub\":\"PS/2022/123\",\"role\":\"PATIENT\",\"iat\":");
        assertThat(payload).contains("\"exp\":");
    }

    @Test
    void expiryIsIssuedAtPlusConfiguredLifetime() {
        Claims claims = jwtService.parseAndValidate(jwtService.generateToken(patient));

        long lifetimeMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(lifetimeMs).isEqualTo(3_600_000);
    }

    @Test
    void parsesItsOwnToken() {
        Claims claims = jwtService.parseAndValidate(jwtService.generateToken(patient));

        assertThat(claims.getSubject()).isEqualTo("PS/2022/123");
        assertThat(claims.get(JwtService.ROLE_CLAIM, String.class)).isEqualTo("PATIENT");
        assertThat(jwtService.extractUsername(jwtService.generateToken(patient))).isEqualTo("PS/2022/123");
    }

    @Test
    void rejectsTokenWhosePayloadWasModified() {
        String[] parts = jwtService.generateToken(patient).split("\\.");
        String adminPayload = decode(parts[1]).replace("PATIENT", "ADMIN");
        String tampered = parts[0] + "." + encode(adminPayload) + "." + parts[2];

        assertThatThrownBy(() -> jwtService.parseAndValidate(tampered)).isInstanceOf(SignatureException.class);
        assertThat(jwtService.isTokenValid(tampered, "PS/2022/123")).isFalse();
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        String forged = Jwts.builder()
                .setSubject("admin")
                .claim("role", "ADMIN")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("an-attacker-secret-that-is-at-least-48-bytes-long!!"
                        .getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS384)
                .compact();

        assertThatThrownBy(() -> jwtService.parseAndValidate(forged)).isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService alreadyExpired = new JwtService(SECRET, -1_000);
        String token = alreadyExpired.generateToken(patient);

        assertThatThrownBy(() -> jwtService.parseAndValidate(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejectsUnsignedAlgNoneToken() {
        String unsigned = encode("{\"alg\":\"none\"}") + "." + encode("{\"sub\":\"admin\",\"role\":\"ADMIN\"}") + ".";

        assertThatThrownBy(() -> jwtService.parseAndValidate(unsigned)).isInstanceOf(JwtException.class);
    }

    @Test
    void refusesASecretThatIsTooShortForHs384() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[32]);

        assertThatThrownBy(() -> new JwtService(shortSecret, 1000)).isInstanceOf(IllegalStateException.class);
    }

    static String decode(String base64Url) {
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }

    static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}
