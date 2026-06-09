package ru.sibmobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sibmobile.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

