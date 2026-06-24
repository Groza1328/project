package ru.sibmobile.dto;

import java.time.LocalDateTime;

public class HomeNotificationDto {

    private final String title;
    private final String message;
    private final String type;
    private final LocalDateTime time;

    public HomeNotificationDto(String title, String message, String type, LocalDateTime time) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
