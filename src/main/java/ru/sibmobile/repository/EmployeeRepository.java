package ru.sibmobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sibmobile.model.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

