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
 * Creates and validates JWTs.
 * <p>
 * Tokens are signed with HMAC-SHA384 (HS384) using a server-side secret. The payload is only
 * Base64URL-encoded (anyone can read it), but it cannot be changed: if a single character of the
 * header or payload is modified, the signature no longer matches and {@link #parseAndValidate}
 * throws {@link io.jsonwebtoken.security.SignatureException}.
 */
@Service
public class JwtService {

    public static final String ROLE_CLAIM = "role";

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

    /** Payload: { "sub": username, "role": ROLE, "iat": issuedAt, "exp": expiry }. */
    public String generateToken(AppUser user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim(ROLE_CLAIM, user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS384)
                .compact();
    }

    /**
     * Verifies the signature and the expiry, then returns the claims.
     *
     * @throws io.jsonwebtoken.security.SignatureException if the token was tampered with
     * @throws io.jsonwebtoken.ExpiredJwtException         if the token has expired
     * @throws JwtException                                for any other malformed/unsupported token
     *                                                     (including unsigned "alg": "none" tokens)
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
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
