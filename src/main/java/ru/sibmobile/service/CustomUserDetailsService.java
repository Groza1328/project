package ru.sibmobile.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sibmobile.model.Employee;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.EmployeeRepository;
import ru.sibmobile.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    public static final String ADMIN_USERNAME = "Admin777";

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .or(() -> userRepository.findByEmail(username))
            .orElse(null);

        if (user != null) {
            if (user.isBlocked()) {
                throw new UsernameNotFoundException("Учётная запись заблокирована");
            }
            if (user.getRestrictedUntil() != null && java.time.LocalDateTime.now().isBefore(user.getRestrictedUntil())) {
                throw new UsernameNotFoundException("Доступ временно ограничен до " + user.getRestrictedUntil());
            }

            List<org.springframework.security.core.GrantedAuthority> authorities = ADMIN_USERNAME.equals(user.getUsername())
                ? List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
                : Collections.emptyList();

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                authorities
            );
        }

        Employee employee = employeeRepository.findByUsername(username)
            .or(() -> employeeRepository.findByEmail(username))
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        String roleName = mapEmployeeRoleToAuthority(employee.getRole());

        return new org.springframework.security.core.userdetails.User(
            employee.getUsername() != null ? employee.getUsername() : employee.getEmail(),
            employee.getPasswordHash(),
            employee.isActive(),
            true, true, true,
            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleName))
        );
    }

    private String mapEmployeeRoleToAuthority(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_EMPLOYEE";
        }
        String normalized = role.trim().toLowerCase();
        return switch (normalized) {
            case "администратор", "admin", "administrator", "роль_admin" -> "ROLE_ADMIN";
            case "менеджер", "manager" -> "ROLE_MANAGER";
            case "механик", "mechanic" -> "ROLE_MECHANIC";
            case "сотрудник", "employee" -> "ROLE_EMPLOYEE";
            default -> normalized.startsWith("role_")
                ? normalized.toUpperCase()
                : "ROLE_EMPLOYEE";
        };
    }
}

