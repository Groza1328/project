package ru.sibmobile.service;

import org.springframework.stereotype.Service;
import ru.sibmobile.dto.AdminUserDto;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.OrderRepository;
import ru.sibmobile.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public List<AdminUserDto> getAllUsersForAdmin() {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.findAll().stream()
                .filter(u -> !CustomUserDetailsService.ADMIN_USERNAME.equals(u.getUsername()))
                .map(u -> toDto(u, now))
                .collect(Collectors.toList());
    }

    private AdminUserDto toDto(User u, LocalDateTime now) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(u.getId());
        String login = u.getUsername();
        if (login == null || login.isBlank()) {
            login = u.getEmail() != null ? u.getEmail() : ("id:" + u.getId());
        }
        dto.setUsername(login);
        dto.setEmail(u.getEmail());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setFullName(u.getFullName());
        dto.setLastLoginAt(u.getLastLoginAt());
        TariffType tariff = u.getTariff() != null ? u.getTariff() : TariffType.STANDARD;
        dto.setTariff(tariff);
        dto.setTariffDisplayName(tariff.name());
        dto.setTariffSince(u.getTariffSince());
        dto.setComplaintsFinesCount(u.getComplaintsFinesCount());
        dto.setBlocked(u.isBlocked());
        dto.setRestrictedUntil(u.getRestrictedUntil());
        dto.setRestrictionReason(u.getRestrictionReason());

        orderRepository.findFirstByUserAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqualOrderByStartDateTimeDesc(u, now, now)
                .ifPresent(order -> {
                    dto.setCurrentCarName(order.getCarType().getDisplayName());
                    dto.setCurrentOrderStart(order.getStartDateTime());
                    dto.setCurrentOrderEnd(order.getEndDateTime());
                });
        return dto;
    }
}
