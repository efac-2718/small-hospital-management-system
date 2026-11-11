package com.example.small_hospital_management_system.repository;

import com.example.small_hospital_management_system.model.AppUser;
import com.example.small_hospital_management_system.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data writes the MongoDB query for each method from its name ("derived queries"):
 * findByUsername(x) becomes  db.users.find({ username: x }).
 */
public interface UserRepository extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Used to list doctors: findByRoleOrderByFullNameAsc(Role.DOCTOR). */
    List<AppUser> findByRoleOrderByFullNameAsc(Role role);

    long countByRole(Role role);
}
