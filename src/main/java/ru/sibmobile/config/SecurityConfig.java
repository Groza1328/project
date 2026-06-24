package ru.sibmobile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.sibmobile.repository.EmployeeRepository;
import ru.sibmobile.repository.UserRepository;
import ru.sibmobile.service.CustomUserDetailsService;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public SecurityConfig(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomUserDetailsService userDetailsService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/forgot-password", "/reset-password", "/verify", "/register", "/privacy").permitAll()
                .requestMatchers(HttpMethod.POST, "/forgot-password", "/reset-password", "/reset-password/**", "/verify", "/verify/**", "/register").permitAll()
                .requestMatchers(
                        "/",
                        "/login",
                        "/privacy",
                        "/verify/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/api/check-username"
                ).permitAll()
                .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .rememberMe(remember -> remember
                .rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService)
                .key("sibmobile-remember-me-key")
                .tokenValiditySeconds(60 * 60 * 24 * 14)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    if (username != null && !username.isBlank()) {
                        request.getSession().setAttribute("loginUsername", username.trim());
                    }
                    response.sendRedirect(request.getContextPath() + "/login?error");
                })
                .successHandler((request, response, authentication) -> {
                    request.getSession().removeAttribute("loginUsername");
                    String username = authentication.getName();
                    LocalDateTime now = LocalDateTime.now();
                    // Обновляем последний вход пользователя
                    userRepository.findByUsername(username).ifPresent(user -> {
                        user.setLastLoginAt(now);
                        userRepository.save(user);
                    });
                    // Если есть сотрудник с таким логином или почтой — тоже обновляем
                    employeeRepository.findByUsername(username)
                        .or(() -> employeeRepository.findByEmail(username))
                        .ifPresent(emp -> {
                            emp.setLastLoginAt(now);
                            employeeRepository.save(emp);
                        });
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                        response.sendRedirect(request.getContextPath() + "/admin");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/home");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            );
        
        return http.build();
    }
}

