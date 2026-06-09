package ru.sibmobile.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sibmobile.model.CarType;

import java.time.LocalDateTime;

public class OrderForm {

    @NotNull
    private CarType carType;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^[+0-9\\s\\-()]{6,20}$", message = "Некорректный номер телефона")
    private String phone;

    @NotBlank(message = "Город обязателен")
    private String city;

    @NotBlank(message = "Адрес обязателен")
    private String address;

    @NotNull(message = "Дата и время начала обязательны")
    @Future(message = "Время начала должно быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @NotNull(message = "Дата и время окончания обязательны")
    @Future(message = "Время окончания должно быть в будущем")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    private boolean sendReceipt;

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isSendReceipt() {
        return sendReceipt;
    }

    public void setSendReceipt(boolean sendReceipt) {
        this.sendReceipt = sendReceipt;
    }
}


