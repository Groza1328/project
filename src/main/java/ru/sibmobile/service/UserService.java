package ru.sibmobile.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private Optional<User> findByEmail(String email) {
        String mail = normalizeEmail(email);
        return userRepository.findByEmailIgnoreCase(mail)
            .or(() -> userRepository.findByEmail(mail));
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return findByEmail(email)
            .map(User::isEnabled)
            .orElse(false);
    }

    public boolean hasPendingVerification(String email) {
        return findByEmail(email)
            .map(user -> !user.isEnabled())
            .orElse(false);
    }

    public boolean isRegisteredEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public String getUsernameByEmail(String email) {
        return findByEmail(email)
            .map(User::getUsername)
            .orElse(null);
    }

    public boolean registerUser(String username, String email, String password) {
        String mail = normalizeEmail(email);
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(mail);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());

        String code = generateVerificationCode();
        user.setVerificationCode(code);
        userRepository.save(user);
        emailService.sendVerificationCodeAsync(mail, code);
        return true;
    }

    public boolean resumePendingRegistration(String username, String email, String password) {
        return findByEmail(email)
            .filter(user -> !user.isEnabled())
            .map(user -> {
                user.setUsername(username.trim());
                user.setPassword(passwordEncoder.encode(password));
                String code = generateVerificationCode();
                user.setVerificationCode(code);
                userRepository.save(user);
                emailService.sendVerificationCodeAsync(user.getEmail(), code);
                return true;
            })
            .orElse(false);
    }

    public boolean verifyUser(String email, String code) {
        String normalizedCode = normalizeCode(code);
        return findByEmail(email)
            .filter(user -> normalizedCode.equals(normalizeCode(user.getVerificationCode())))
            .map(user -> {
                user.setEnabled(true);
                user.setVerificationCode(null);
                userRepository.save(user);
                return true;
            })
            .orElse(false);
    }

    public boolean resendVerificationCode(String email) {
        return findByEmail(email)
            .filter(user -> !user.isEnabled())
            .map(user -> {
                String code = generateVerificationCode();
                user.setVerificationCode(code);
                userRepository.save(user);
                emailService.sendVerificationCodeAsync(user.getEmail(), code);
                return true;
            })
            .orElse(false);
    }

    public boolean startPasswordReset(String email) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        String code = generateVerificationCode();
        user.setResetCode(code);
        userRepository.save(user);
        emailService.sendPasswordResetCodeAsync(user.getEmail(), code);
        return true;
    }

    public boolean resendPasswordResetCode(String email) {
        return startPasswordReset(email);
    }

    public boolean resetPassword(String email, String code, String newPassword) {
        String normalizedCode = normalizeCode(code);
        return findByEmail(email)
            .filter(user -> normalizedCode.equals(normalizeCode(user.getResetCode())))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetCode(null);
                user.setEnabled(true);
                userRepository.save(user);
                return true;
            })
            .orElse(false);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        String digits = code.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return "";
        }
        if (digits.length() > 6) {
            digits = digits.substring(digits.length() - 6);
        }
        return String.format("%06d", Integer.parseInt(digits));
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
