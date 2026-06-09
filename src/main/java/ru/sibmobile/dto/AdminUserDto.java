package ru.sibmobile.dto;

import ru.sibmobile.model.TariffType;

import java.time.LocalDateTime;

/** Строка таблицы пользователей для админ-панели */
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    /** Полное имя пользователя (ФИО) */
    private String fullName;
    /** Последний вход в систему */
    private LocalDateTime lastLoginAt;
    private TariffType tariff;
    /** Название подписки для отображения (всегда не null) */
    private String tariffDisplayName;
    private LocalDateTime tariffSince;
    /** Название машины текущего проката или null */
    private String currentCarName;
    /** Дата оформления текущего проката */
    private LocalDateTime currentOrderStart;
    /** Окончание проката (срок) */
    private LocalDateTime currentOrderEnd;
    private int complaintsFinesCount;
    private boolean blocked;
    private LocalDateTime restrictedUntil;
    private String restrictionReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public TariffType getTariff() { return tariff; }
    public void setTariff(TariffType tariff) { this.tariff = tariff; }
    public String getTariffDisplayName() { return tariffDisplayName; }
    public void setTariffDisplayName(String tariffDisplayName) { this.tariffDisplayName = tariffDisplayName; }
    public LocalDateTime getTariffSince() { return tariffSince; }
    public void setTariffSince(LocalDateTime tariffSince) { this.tariffSince = tariffSince; }
    public String getCurrentCarName() { return currentCarName; }
    public void setCurrentCarName(String currentCarName) { this.currentCarName = currentCarName; }
    public LocalDateTime getCurrentOrderStart() { return currentOrderStart; }
    public void setCurrentOrderStart(LocalDateTime currentOrderStart) { this.currentOrderStart = currentOrderStart; }
    public LocalDateTime getCurrentOrderEnd() { return currentOrderEnd; }
    public void setCurrentOrderEnd(LocalDateTime currentOrderEnd) { this.currentOrderEnd = currentOrderEnd; }
    public int getComplaintsFinesCount() { return complaintsFinesCount; }
    public void setComplaintsFinesCount(int complaintsFinesCount) { this.complaintsFinesCount = complaintsFinesCount; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public LocalDateTime getRestrictedUntil() { return restrictedUntil; }
    public void setRestrictedUntil(LocalDateTime restrictedUntil) { this.restrictedUntil = restrictedUntil; }
    public String getRestrictionReason() { return restrictionReason; }
    public void setRestrictionReason(String restrictionReason) { this.restrictionReason = restrictionReason; }
}
