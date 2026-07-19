package com.hrms.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Security loadUserByUsername calls this during authentication
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}