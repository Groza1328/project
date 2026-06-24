package ru.sibmobile.service;

import org.springframework.stereotype.Service;
import ru.sibmobile.dto.HomeDashboardDto;
import ru.sibmobile.dto.HomeNotificationDto;
import ru.sibmobile.dto.HomeOrderDto;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.Order;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class UserDashboardService {

    private final OrderRepository orderRepository;

    public UserDashboardService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public HomeDashboardDto buildDashboard(User user) {
        HomeDashboardDto dashboard = new HomeDashboardDto();
        dashboard.setUsername(user.getUsername());
        dashboard.setEmail(user.getEmail());
        dashboard.setTariff(formatTariff(user.getTariff()));
        dashboard.setRegisteredAt(user.getCreatedAt());
        dashboard.setLastLoginAt(user.getLastLoginAt());
        dashboard.setComplaintsCount(user.getComplaintsFinesCount());

        LocalDateTime now = LocalDateTime.now();
        for (Order order : orderRepository.findByUserWithCarOrderByCreatedAtDesc(user)) {
            dashboard.getOrders().add(toOrderDto(order, now));
        }

        addAccountNotifications(dashboard, user, now);
        addOrderNotifications(dashboard, now);

        dashboard.getNotifications().sort(
                Comparator.comparing(HomeNotificationDto::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return dashboard;
    }

    private void addAccountNotifications(HomeDashboardDto dashboard, User user, LocalDateTime now) {
        if (user.isBlocked()) {
            String reason = user.getRestrictionReason();
            dashboard.getNotifications().add(new HomeNotificationDto(
                    "Аккаунт заблокирован",
                    reason != null && !reason.isBlank()
                            ? "Причина: " + reason
                            : "Обратитесь в поддержку для разблокировки.",
                    "warning",
                    now));
        }

        if (user.getRestrictedUntil() != null && user.getRestrictedUntil().isAfter(now)) {
            dashboard.getNotifications().add(new HomeNotificationDto(
                    "Временное ограничение",
                    "Доступ ограничен до " + formatDateTime(user.getRestrictedUntil()) + ".",
                    "warning",
                    user.getRestrictedUntil()));
        }

        TariffType tariff = user.getTariff() != null ? user.getTariff() : TariffType.STANDARD;
        if (tariff != TariffType.STANDARD) {
            dashboard.getNotifications().add(new HomeNotificationDto(
                    "Подписка активна",
                    "Ваш тариф: " + formatTariff(tariff) + ".",
                    "info",
                    user.getTariffSince() != null ? user.getTariffSince() : now));
        }

        if (user.getComplaintsFinesCount() > 0) {
            dashboard.getNotifications().add(new HomeNotificationDto(
                    "Штрафы и жалобы",
                    "Зафиксировано нарушений: " + user.getComplaintsFinesCount() + ".",
                    "warning",
                    now));
        }

        dashboard.getNotifications().add(new HomeNotificationDto(
                "Добро пожаловать",
                "Вы вошли как " + user.getUsername() + " (" + user.getEmail() + ").",
                "info",
                user.getLastLoginAt() != null ? user.getLastLoginAt() : now));
    }

    private void addOrderNotifications(HomeDashboardDto dashboard, LocalDateTime now) {
        for (HomeOrderDto order : dashboard.getOrders()) {
            switch (order.getStatus()) {
                case "ACTIVE" -> dashboard.getNotifications().add(new HomeNotificationDto(
                        "Активный прокат",
                        order.getCarName() + ", госномер " + plateLabel(order) + ", до "
                                + formatDateTime(order.getEndDateTime()) + ".",
                        "order",
                        order.getEndDateTime()));
                case "UPCOMING" -> dashboard.getNotifications().add(new HomeNotificationDto(
                        "Предстоящий заказ",
                        order.getCarName() + ", госномер " + plateLabel(order) + ", с "
                                + formatDateTime(order.getStartDateTime()) + ".",
                        "order",
                        order.getStartDateTime()));
                case "COMPLETED" -> dashboard.getNotifications().add(new HomeNotificationDto(
                        "Завершённый прокат",
                        order.getCarName() + ", госномер " + plateLabel(order) + ".",
                        "order",
                        order.getEndDateTime()));
                default -> {
                }
            }
        }

        if (dashboard.getOrders().isEmpty()) {
            dashboard.getNotifications().add(new HomeNotificationDto(
                    "Нет заказов",
                    "Вы ещё не оформляли прокат. Перейдите в раздел «Заказать».",
                    "info",
                    now));
        }
    }

    private HomeOrderDto toOrderDto(Order order, LocalDateTime now) {
        String status;
        String statusLabel;
        if (!now.isBefore(order.getStartDateTime()) && !now.isAfter(order.getEndDateTime())) {
            status = "ACTIVE";
            statusLabel = "Активен";
        } else if (now.isBefore(order.getStartDateTime())) {
            status = "UPCOMING";
            statusLabel = "Предстоящий";
        } else {
            status = "COMPLETED";
            statusLabel = "Завершён";
        }

        Car car = order.getCar();
        String plate = car != null ? car.getPlateNumber() : "—";
        String city = car != null ? car.getCity() : "—";

        return new HomeOrderDto(
                order.getId(),
                order.getCarType().getDisplayName(),
                plate,
                city,
                order.getStartDateTime(),
                order.getEndDateTime(),
                order.getCreatedAt(),
                status,
                statusLabel);
    }

    private String plateLabel(HomeOrderDto order) {
        if (order.getPlateNumber() == null || order.getPlateNumber().isBlank()) {
            return "не назначен";
        }
        return order.getPlateNumber();
    }

    private String formatTariff(TariffType tariff) {
        if (tariff == null) {
            return "Стандарт";
        }
        return switch (tariff) {
            case STANDARD -> "Стандарт";
            case ECONOMY -> "Эконом";
            case PREMIUM -> "Премиум";
            case LUX -> "Люкс";
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
