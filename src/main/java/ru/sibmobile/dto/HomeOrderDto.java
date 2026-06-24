package ru.sibmobile.dto;

import java.time.LocalDateTime;

public class HomeOrderDto {

    private final Long id;
    private final String carName;
    private final String plateNumber;
    private final String city;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final LocalDateTime createdAt;
    private final String status;
    private final String statusLabel;

    public HomeOrderDto(Long id,
                        String carName,
                        String plateNumber,
                        String city,
                        LocalDateTime startDateTime,
                        LocalDateTime endDateTime,
                        LocalDateTime createdAt,
                        String status,
                        String statusLabel) {
        this.id = id;
        this.carName = carName;
        this.plateNumber = plateNumber;
        this.city = city;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createdAt = createdAt;
        this.status = status;
        this.statusLabel = statusLabel;
    }

    public Long getId() {
        return id;
    }

    public String getCarName() {
        return carName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
