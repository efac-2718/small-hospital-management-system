package com.example.small_hospital_management_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request: reads "Authorization: Bearer &lt;JWT&gt;", verifies the token's signature
 * and expiry, loads the user from the database and logs them in for this request only (stateless).
 * <p>
 * A request with no Bearer header passes through as anonymous (public URLs still work, protected
 * ones get a 401 from {@link JsonAuthenticationEntryPoint}). A request that DOES send a token which
 * is tampered, expired or malformed is rejected immediately with 401 - it is never treated as anonymous.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_PATH = "/api/auth/login";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ErrorResponseWriter errorResponseWriter;

    /** Logging in never needs a token, so a stale token left in the header must not block it. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return LOGIN_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            reject(request, response, "Missing JWT after 'Bearer '");
            return;
        }

        try {
            // 1. Verify signature + expiry. A modified payload (e.g. role PATIENT -> ADMIN) fails here.
            Claims claims = jwtService.parseAndValidate(token);

            // 2. Load the user from the database; roles always come from the database.
            UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
            if (!user.isEnabled()) {
                reject(request, response, "This account is disabled");
                return;
            }

            // 3. Log the user in for this request only.
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (SignatureException e) {
            log.warn("Rejected JWT with invalid signature from {} on {}", request.getRemoteAddr(),
                    request.getRequestURI());
            reject(request, response,
                    "Invalid JWT signature: the token has been modified or was not issued by this server");
            return;
        } catch (ExpiredJwtException e) {
            reject(request, response, "JWT token has expired, please log in again");
            return;
        } catch (UsernameNotFoundException e) {
            reject(request, response, "The user in this JWT no longer exists");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            reject(request, response, "Invalid JWT token: malformed or unsupported");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        SecurityContextHolder.clearContext();
        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, message);
    }
}
