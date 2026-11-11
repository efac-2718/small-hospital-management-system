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
 * LAB 5 - The JWT filter: the heart of stateless authentication.
 *
 * Every HTTP request passes through this filter BEFORE it reaches a controller. Because the server
 * keeps no session, the token in the request is the ONLY thing that says who the caller is.
 *
 * Check your work:   ./mvnw test -Dtest=JwtAuthenticationFilterTest
 * Then the full app:  ./mvnw test -Dtest=SecurityFlowIntegrationTests
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

    /** Logging in never needs a token, so a stale token left in the header must not block it. (done) */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return LOGIN_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // TODO 1: read the "Authorization" header (HttpHeaders.AUTHORIZATION).
        //         If it is missing or does not start with "Bearer ", this is an anonymous request:
        //         call filterChain.doFilter(request, response) and return.
        //         (Public URLs still work; protected URLs will get a 401 later from JsonAuthenticationEntryPoint.)

        // TODO 2: cut off the "Bearer " prefix to get the token.

        // TODO 3: inside a try block:
        //         a) Claims claims = jwtService.parseAndValidate(token);   <- signature + expiry check
        //         b) UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
        //            (roles come from the DATABASE, not from the token)
        //         c) create the "logged in" object:
        //              UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities())
        //            and optionally setDetails(new WebAuthenticationDetailsSource().buildDetails(request))
        //         d) put it in a new SecurityContext and store it:
        //              SecurityContext context = SecurityContextHolder.createEmptyContext();
        //              context.setAuthentication(...);
        //              SecurityContextHolder.setContext(context);

        // TODO 4: catch the problems and answer 401 with reject(...), then RETURN (do not call the chain!):
        //         SignatureException        -> "Invalid JWT signature: the token has been modified or was not issued by this server"
        //         ExpiredJwtException       -> "JWT token has expired, please log in again"
        //         UsernameNotFoundException -> "The user in this JWT no longer exists"
        //         JwtException | IllegalArgumentException -> "Invalid JWT token: malformed or unsupported"
        //         Order matters: SignatureException and ExpiredJwtException ARE JwtExceptions, so catch them first.
        //
        // Question: why reject a bad token with 401 instead of just continuing as an anonymous user?

        // TODO 5: everything is fine - call the next filter.
        throw new UnsupportedOperationException("TODO Lab 5: doFilterInternal");
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        SecurityContextHolder.clearContext();
        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, message);
    }
}
