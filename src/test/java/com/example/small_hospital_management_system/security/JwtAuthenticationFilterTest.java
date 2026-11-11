package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Lab 5 checker: the JWT filter. Uses Spring's mock request/response, so no server or database runs.
 */
class JwtAuthenticationFilterTest {

    private final JwtService jwtService = new JwtService(JwtServiceTest.SECRET, 3_600_000);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtService, new CustomUserDetailsService(userRepository), new ErrorResponseWriter(new JsonMapper()));

    private final AppUser alice = AppUser.builder()
            .id("u1").username("alice").password("$2a$10$hash").fullName("Alice").role(Role.PATIENT).enabled(true)
            .build();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithoutTokenPassesThroughAsAnonymous() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/patients", null), response, chain);

        assertThat(chain.getRequest()).as("next filter was called").isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validTokenLogsTheUserInWithTheRoleFromTheDatabase() throws Exception {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/patients/me", jwtService.generateToken(alice)),
                new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).as("next filter was called").isNotNull();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice");
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_PATIENT");
    }

    @Test
    void tamperedTokenIsRejectedWith401AndNeverReachesTheController() throws Exception {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        String[] parts = jwtService.generateToken(alice).split("\\.");
        String tampered = parts[0] + "."
                + JwtServiceTest.encode(JwtServiceTest.decode(parts[1]).replace("PATIENT", "ADMIN"))
                + "." + parts[2];
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/auth/register", tampered), response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid JWT signature");
        assertThat(chain.getRequest()).as("next filter must NOT be called").isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void expiredTokenIsRejectedWith401() throws Exception {
        String expired = new JwtService(JwtServiceTest.SECRET, -1_000).generateToken(alice);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/patients", expired), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("expired");
    }

    @Test
    void tokenOfADeletedUserIsRejectedWith401() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/patients", jwtService.generateToken(alice)), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void garbageTokenIsRejectedWith401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/patients", "not-a-jwt"), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
    }

    @Test
    void loginIgnoresAnOldTokenInTheHeader() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request("/api/auth/login", "old.or.broken"), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).as("login must still work").isNotNull();
    }

    private static MockHttpServletRequest request(String uri, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        return request;
    }
}
