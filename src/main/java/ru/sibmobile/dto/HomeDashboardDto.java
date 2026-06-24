package ru.sibmobile.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeDashboardDto {

    private String username;
    private String email;
    private String tariff;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private int complaintsCount;
    private final List<HomeNotificationDto> notifications = new ArrayList<>();
    private final List<HomeOrderDto> orders = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getComplaintsCount() {
        return complaintsCount;
    }

    public void setComplaintsCount(int complaintsCount) {
        this.complaintsCount = complaintsCount;
    }

    public List<HomeNotificationDto> getNotifications() {
        return notifications;
    }

    public List<HomeOrderDto> getOrders() {
        return orders;
    }

    public int getNotificationCount() {
        return notifications.size();
    }
}
