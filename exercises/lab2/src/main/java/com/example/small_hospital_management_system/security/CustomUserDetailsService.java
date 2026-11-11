package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * LAB 2 - How Spring Security finds a user.
 *
 * Spring Security does not know about MongoDB or AppUser. It only calls
 * {@link UserDetailsService#loadUserByUsername}. Your job is to translate our AppUser document
 * into Spring Security's {@link UserDetails}.
 *
 * Who calls this?
 *   - On login:  DaoAuthenticationProvider loads the user, then BCrypt-compares the typed password
 *                with getPassword() (the hash). See SecurityConfig.authenticationManager().
 *   - On every request with a JWT: JwtAuthenticationFilter loads the user named in the token.
 *
 * Check your work:   ./mvnw test -Dtest=CustomUserDetailsServiceTest
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO 1: find the AppUser by username; if missing throw new UsernameNotFoundException(...)
        // TODO 2: build and return a UserDetails with
        //           User.withUsername(...)
        //               .password(...)                         <- the BCrypt HASH, never plain text
        //               .authorities(user.getRole().authority()) <- e.g. "ROLE_DOCTOR"
        //               .disabled(...)
        //               .build();
        // Question: why "ROLE_DOCTOR" and not "DOCTOR"? (look at hasRole('DOCTOR') in the controllers)
        throw new UnsupportedOperationException("TODO Lab 2: loadUserByUsername");
    }
}
