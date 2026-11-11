package com.example.small_hospital_management_system.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * LAB 6 - The SecurityFilterChain: the rules for the whole application.
 *
 * Check your work:   ./mvnw test -Dtest=SecurityFlowIntegrationTests
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // switches on @PreAuthorize (Lab 7). What happens to RBAC if you delete this line?
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO 1: we use tokens in a header, not cookies, so turn OFF:
        //           http.csrf(AbstractHttpConfigurer::disable)
        //           .httpBasic(...disable) .formLogin(...disable) .logout(...disable)
        //         Question: why is CSRF protection not needed when the browser never sends the token automatically?

        // TODO 2: make the API stateless - no HttpSession, no JSESSIONID cookie:
        //           .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // TODO 3: send our JSON errors instead of Spring's defaults:
        //           .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)   // 401
        //                                      .accessDeniedHandler(accessDeniedHandler))            // 403

        // TODO 4: URL rules (first match wins, so the most specific rules go first):
        //           POST /api/auth/login and POST /api/auth/register -> permitAll()
        //           /error                                            -> permitAll()
        //           /api/admin/**                                     -> hasRole("ADMIN")
        //           anything else                                     -> authenticated()
        //         Hint: .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "...", "...").permitAll() ... )

        // TODO 5: run OUR JWT filter before Spring's username/password filter:
        //           .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        //         Question: what would happen if the JWT filter ran AFTER the authorization check?

        return http.build();
    }

    /** BCrypt with the default cost (10). See Lab 2. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Checks username + password on login: loads the user, then BCrypt-compares the password. */
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * The JWT filter is a @Component, so Spring Boot would ALSO register it as a plain servlet filter.
     * Disable that: it must run only once, inside the Spring Security filter chain.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
