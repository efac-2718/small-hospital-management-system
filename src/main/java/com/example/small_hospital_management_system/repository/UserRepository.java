package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Used to list doctors: findByRoleOrderByFullNameAsc(Role.DOCTOR). */
    List<AppUser> findByRoleOrderByFullNameAsc(Role role);

    long countByRole(Role role);
}
