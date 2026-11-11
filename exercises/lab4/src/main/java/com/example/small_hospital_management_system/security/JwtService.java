package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * LAB 4 - Creating and validating JWTs with JJWT 0.11.5.
 *
 * Check your work:   ./mvnw test -Dtest=JwtServiceTest
 */
@Service
public class JwtService {

    public static final String ROLE_CLAIM = "role";

    /** The HMAC key made from app.jwt.secret. Whoever has this key can create valid tokens! */
    private final Key signingKey;

    @Getter
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        if (keyBytes.length < 48) {
            throw new IllegalStateException(
                    "app.jwt.secret must be a Base64 value of at least 48 bytes (384 bits) for HS384");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * TODO 1: build a token whose payload is  { "sub": username, "role": ROLE, "iat": now, "exp": now + expirationMs }
     *         signed with signingKey using HS384.
     *
     * Hint:   Jwts.builder()
     *             .setSubject(...)
     *             .claim(ROLE_CLAIM, ...)          <- user.getRole().name()
     *             .setIssuedAt(...)                <- a java.util.Date
     *             .setExpiration(...)
     *             .signWith(signingKey, SignatureAlgorithm.HS384)
     *             .compact();
     *
     * Order matters for the test: sub, role, iat, exp.
     * Question: is the payload encrypted? (paste a token into jwt.io and see)
     */
    public String generateToken(AppUser user) {
        throw new UnsupportedOperationException("TODO Lab 4: generateToken");
    }

    /**
     * TODO 2: verify the signature AND the expiry, then return the claims (the payload).
     *
     * Hint:   Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
     *
     * JJWT throws for you:
     *   SignatureException   - the token was modified, or signed with a different key
     *   ExpiredJwtException  - "exp" is in the past
     *   UnsupportedJwtException / MalformedJwtException - "alg":"none" tokens, garbage, ...
     * Question: why must you use parseClaimsJws (with an "s") and not parseClaimsJwt?
     */
    public Claims parseAndValidate(String token) {
        throw new UnsupportedOperationException("TODO Lab 4: parseAndValidate");
    }

    public String extractUsername(String token) {
        return parseAndValidate(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseAndValidate(token);
            return expectedUsername.equals(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
