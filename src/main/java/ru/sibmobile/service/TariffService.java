package ru.sibmobile.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.sibmobile.model.CarType;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.UserRepository;

@Service
public class TariffService {

    private final UserRepository userRepository;

    public TariffService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public TariffType getCurrentTariff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return TariffType.STANDARD;
        }
        String login = auth.getName();
        return findByLoginOrEmail(login)
                .map(user -> user.getTariff() != null ? user.getTariff() : TariffType.STANDARD)
                .orElse(TariffType.STANDARD);
    }

    public void changeTariff(TariffType type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }
        String login = auth.getName();
        findByLoginOrEmail(login).ifPresent(user -> {
            user.setTariff(type);
            user.setTariffSince(type == TariffType.STANDARD ? null : java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }

    public double getPrepaymentMultiplier(TariffType type) {
        return switch (type) {
            case ECONOMY -> 0.75; // 25% скидка
            case PREMIUM -> 0.65; // 35% скидка
            case LUX -> 0.5;      // 50% скидка
            default -> 1.0;
        };
    }

    public double getPerKmMultiplier(TariffType type) {
        return switch (type) {
            case PREMIUM -> 0.65; // 35% скидка
            case LUX -> 0.5;      // 50% скидка
            default -> 1.0;       // Стандарт и Эконом без скидки на километр
        };
    }

    public boolean canFreeParkingAnywhere(TariffType type) {
        return type == TariffType.LUX;
    }

    public CarPrice calculatePrice(TariffType tariff, CarType carType) {
        double basePre = carType.getBasePrepayment();
        double baseKm = carType.getBasePerKm();
        double pre = Math.round(basePre * getPrepaymentMultiplier(tariff));
        double km = round2(baseKm * getPerKmMultiplier(tariff));
        return new CarPrice(basePre, baseKm, pre, km);
    }

    public CarPrice calculatePriceForCurrentUser(CarType carType) {
        TariffType t = getCurrentTariff();
        return calculatePrice(t, carType);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private java.util.Optional<User> findByLoginOrEmail(String login) {
        return userRepository.findByUsername(login).or(() -> userRepository.findByEmail(login));
    }

    public static class CarPrice {
        private final double basePrepayment;
        private final double basePerKm;
        private final double prepayment;
        private final double perKm;

        public CarPrice(double basePrepayment, double basePerKm, double prepayment, double perKm) {
            this.basePrepayment = basePrepayment;
            this.basePerKm = basePerKm;
            this.prepayment = prepayment;
            this.perKm = perKm;
        }

        public double getBasePrepayment() {
            return basePrepayment;
        }

        public double getBasePerKm() {
            return basePerKm;
        }

        public double getPrepayment() {
            return prepayment;
        }

        public double getPerKm() {
            return perKm;
        }
    }
}


