package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import com.example.small_hospital_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Lab 2 checker: loading a user, their BCrypt hash and their role from MongoDB. */
class CustomUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

    @Test
    void loadsUsernameHashAndRoleAsAuthority() {
        when(userRepository.findByUsername("dr.silva")).thenReturn(Optional.of(AppUser.builder()
                .username("dr.silva").password("$2a$10$abcdefghijklmnopqrstuv").role(Role.DOCTOR).enabled(true)
                .build()));

        UserDetails user = service.loadUserByUsername("dr.silva");

        assertThat(user.getUsername()).isEqualTo("dr.silva");
        assertThat(user.getPassword()).isEqualTo("$2a$10$abcdefghijklmnopqrstuv");
        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_DOCTOR");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void disabledAccountIsReportedAsDisabled() {
        when(userRepository.findByUsername("old")).thenReturn(Optional.of(AppUser.builder()
                .username("old").password("x").role(Role.NURSE).enabled(false).build()));

        assertThat(service.loadUserByUsername("old").isEnabled()).isFalse();
    }

    @Test
    void unknownUserThrowsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost")).isInstanceOf(UsernameNotFoundException.class);
    }
}
