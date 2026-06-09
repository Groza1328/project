package ru.sibmobile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть минимум 6 символов")
    private String password;

    /** Полное имя (ФИО) для отображения */
    private String fullName;

    /** Роль для админ-панели (USER/EMPLOYEE/ADMIN и т.п.) */
    private String role;

    /** Дата и время последнего входа в систему */
    private LocalDateTime lastLoginAt;

    private boolean enabled = false;
    
    private String verificationCode;
    
    // Одноразовый код для восстановления пароля
    private String resetCode;

    @Enumerated(EnumType.STRING)
    private TariffType tariff = TariffType.STANDARD;

    /** Дата регистрации */
    private LocalDateTime createdAt;

    /** Дата начала действия текущей подписки (для не STANDARD); для STANDARD — null */
    private LocalDateTime tariffSince;

    /** Заблокирован навсегда (до разблокировки админом) */
    @Column(columnDefinition = "boolean default false")
    private boolean blocked = false;

    /** Ограничен до указанной даты */
    private LocalDateTime restrictedUntil;

    /** Причина блокировки или ограничения */
    private String restrictionReason;

    /** Количество жалоб/штрафов */
    @Column(columnDefinition = "integer default 0")
    private int complaintsFinesCount = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }

    public TariffType getTariff() { return tariff; }
    public void setTariff(TariffType tariff) { this.tariff = tariff; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTariffSince() { return tariffSince; }
    public void setTariffSince(LocalDateTime tariffSince) { this.tariffSince = tariffSince; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public LocalDateTime getRestrictedUntil() { return restrictedUntil; }
    public void setRestrictedUntil(LocalDateTime restrictedUntil) { this.restrictedUntil = restrictedUntil; }

    public String getRestrictionReason() { return restrictionReason; }
    public void setRestrictionReason(String restrictionReason) { this.restrictionReason = restrictionReason; }

    public int getComplaintsFinesCount() { return complaintsFinesCount; }
    public void setComplaintsFinesCount(int complaintsFinesCount) { this.complaintsFinesCount = complaintsFinesCount; }
}

