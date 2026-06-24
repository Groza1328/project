package ru.sibmobile.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.CarType;
import ru.sibmobile.model.Employee;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.CarRepository;
import ru.sibmobile.repository.EmployeeRepository;
import ru.sibmobile.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    private static final String ADMIN_USERNAME = "Admin777";
    private static final String ADMIN_PASSWORD = "Admin123";
    private static final String ADMIN_EMAIL = "admin@sibmobile.local";

    @Bean
    public ApplicationRunner initAdmin(UserRepository userRepository,
                                       EmployeeRepository employeeRepository,
                                       CarRepository carRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // Создаём админа
            if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
                User admin = new User();
                admin.setUsername(ADMIN_USERNAME);
                admin.setEmail(ADMIN_EMAIL);
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
            }

            // Исправляем пользователей с пустым логином (подставляем email или user_id)
            userRepository.findAll().forEach(user -> {
                if (user.getUsername() == null || user.getUsername().isBlank()) {
                    String newLogin = (user.getEmail() != null && !user.getEmail().isBlank())
                        ? user.getEmail()
                        : ("user_" + user.getId());
                    user.setUsername(newLogin);
                    userRepository.save(user);
                }
                if (user.getTariff() == null) {
                    user.setTariff(TariffType.STANDARD);
                    userRepository.save(user);
                }
            });

            // Создаём тестовых пользователей (если только админ)
            if (userRepository.count() == 1) {
                createTestUser(userRepository, passwordEncoder, "ivanov", "ivanov@example.com", 
                    TariffType.ECONOMY, LocalDateTime.now().minusDays(30), 0);
                createTestUser(userRepository, passwordEncoder, "petrova", "petrova@example.com", 
                    TariffType.PREMIUM, LocalDateTime.now().minusDays(15), 1);
                createTestUser(userRepository, passwordEncoder, "sidorov", "sidorov@example.com", 
                    TariffType.LUX, LocalDateTime.now().minusDays(7), 0);
                createTestUser(userRepository, passwordEncoder, "kuznetsova", "kuznetsova@example.com", 
                    TariffType.STANDARD, LocalDateTime.now().minusDays(45), 2);
            }

            // Если сотрудники без логина/пароля — пересоздаём таблицу сотрудников с полными данными
            boolean needEmployeesReset = employeeRepository.count() == 0 ||
                employeeRepository.findAll().stream().anyMatch(e ->
                    e.getUsername() == null || e.getUsername().isBlank() ||
                    e.getPasswordHash() == null || e.getPasswordHash().isBlank() ||
                    e.getRole() == null || e.getRole().isBlank() ||
                    e.getRole().equalsIgnoreCase("EMPLOYEE") ||
                    e.getRole().equalsIgnoreCase("MANAGER") ||
                    e.getRole().equalsIgnoreCase("MECHANIC") ||
                    e.getRole().equalsIgnoreCase("ADMIN") ||
                    e.getFullName() == null || e.getFullName().isBlank()
                );

            if (needEmployeesReset) {
                employeeRepository.deleteAll();

                createEmployee(employeeRepository, passwordEncoder,
                    "manager", "Manager123!",
                    "Иван Петров", "Менеджер по работе с клиентами",
                    "manager@sibmobile.local", "+7 (900) 000-00-01",
                    "Менеджер", LocalDateTime.now().minusMonths(6));

                createEmployee(employeeRepository, passwordEncoder,
                    "mechanic", "Mechanic123!",
                    "Сергей Кузнецов", "Механик автопарка",
                    "mechanic@sibmobile.local", "+7 (900) 000-00-02",
                    "Механик", LocalDateTime.now().minusMonths(3));

                createEmployee(employeeRepository, passwordEncoder,
                    "support", "Support123!",
                    "Анна Соколова", "Служба поддержки",
                    "support@sibmobile.local", "+7 (900) 000-00-03",
                    "Сотрудник", LocalDateTime.now().minusMonths(2));
            }

            // Автопарк: по 2 одинаковых авто в каждом городе (регионы 55/155 — Омск, 54/154 — Новосибирск)
            ensureFleet(carRepository);
        };
    }

    private void createTestUser(UserRepository repo, PasswordEncoder encoder, 
                                String username, String email, TariffType tariff, 
                                LocalDateTime createdAt, int complaints) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode("password123"));
        user.setEnabled(true);
        user.setCreatedAt(createdAt);
        user.setTariff(tariff);
        user.setTariffSince(tariff == TariffType.STANDARD ? null : createdAt.plusDays(5));
        user.setComplaintsFinesCount(complaints);
        repo.save(user);
    }

    private void createEmployee(EmployeeRepository repo,
                                PasswordEncoder encoder,
                                String username,
                                String rawPassword,
                                String fullName,
                                String position,
                                String email,
                                String phone,
                                String role,
                                LocalDateTime hiredAt) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPasswordHash(encoder.encode(rawPassword));
        employee.setFullName(fullName);
        employee.setPosition(position);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setRole(role);
        employee.setActive(true);
        employee.setHiredAt(hiredAt);
        repo.save(employee);
    }

    private void ensureFleet(CarRepository carRepository) {
        seedCarIfMissing(carRepository, CarType.SOLARIS, "Омск", "А000АА55",
                "Hyundai Solaris (2020–2023)", "Седан", "/images/Auto1.jpg",
                "Надёжный городской седан с экономичным расходом топлива.");
        seedCarIfMissing(carRepository, CarType.SOLARIS, "Омск", "К101КК155",
                "Hyundai Solaris (2020–2023)", "Седан", "/images/Auto1.jpg",
                "Надёжный городской седан с экономичным расходом топлива.");
        seedCarIfMissing(carRepository, CarType.SOLARIS, "Новосибирск", "А000АА154",
                "Hyundai Solaris (2020–2023)", "Седан", "/images/Auto1.jpg",
                "Надёжный городской седан с экономичным расходом топлива.");
        seedCarIfMissing(carRepository, CarType.SOLARIS, "Новосибирск", "К101КК54",
                "Hyundai Solaris (2020–2023)", "Седан", "/images/Auto1.jpg",
                "Надёжный городской седан с экономичным расходом топлива.");

        seedCarIfMissing(carRepository, CarType.GEELY_XINGYUE, "Омск", "А456ВЕ55",
                "Geely Xingyue S (Xingyue L)", "Кроссовер", "/images/Auto2.jpg",
                "Мощный кроссовер с продвинутыми ассистентами и полным приводом.");
        seedCarIfMissing(carRepository, CarType.GEELY_XINGYUE, "Омск", "М378РО155",
                "Geely Xingyue S (Xingyue L)", "Кроссовер", "/images/Auto2.jpg",
                "Мощный кроссовер с продвинутыми ассистентами и полным приводом.");
        seedCarIfMissing(carRepository, CarType.GEELY_XINGYUE, "Новосибирск", "В111ВВ154",
                "Geely Xingyue S (Xingyue L)", "Кроссовер", "/images/Auto2.jpg",
                "Мощный кроссовер с продвинутыми ассистентами и полным приводом.");
        seedCarIfMissing(carRepository, CarType.GEELY_XINGYUE, "Новосибирск", "А456ВЕ154",
                "Geely Xingyue S (Xingyue L)", "Кроссовер", "/images/Auto2.jpg",
                "Мощный кроссовер с продвинутыми ассистентами и полным приводом.");

        seedCarIfMissing(carRepository, CarType.NISSAN_QASHQAI, "Омск", "С222СС55",
                "Nissan Qashqai II (J11)", "Кроссовер", "/images/Auto3.jpg",
                "Популярный городской кроссовер, комфортный и практичный.");
        seedCarIfMissing(carRepository, CarType.NISSAN_QASHQAI, "Омск", "Н333НН155",
                "Nissan Qashqai II (J11)", "Кроссовер", "/images/Auto3.jpg",
                "Популярный городской кроссовер, комфортный и практичный.");
        seedCarIfMissing(carRepository, CarType.NISSAN_QASHQAI, "Новосибирск", "С222СС154",
                "Nissan Qashqai II (J11)", "Кроссовер", "/images/Auto3.jpg",
                "Популярный городской кроссовер, комфортный и практичный.");
        seedCarIfMissing(carRepository, CarType.NISSAN_QASHQAI, "Новосибирск", "Н333НН54",
                "Nissan Qashqai II (J11)", "Кроссовер", "/images/Auto3.jpg",
                "Популярный городской кроссовер, комфортный и практичный.");

        carRepository.findAll().forEach(car -> {
            boolean changed = false;
            if (car.getBodyType() == null || car.getBodyType().isBlank()) {
                car.setBodyType(switch (car.getType()) {
                    case SOLARIS -> "Седан";
                    case GEELY_XINGYUE, NISSAN_QASHQAI -> "Кроссовер";
                });
                changed = true;
            }
            if (car.getCity() == null || car.getCity().isBlank()) {
                car.setCity(inferCityByPlate(car.getPlateNumber()));
                changed = true;
            }
            if (changed) {
                carRepository.save(car);
            }
        });
    }

    private void seedCarIfMissing(CarRepository carRepository,
                                  CarType type,
                                  String city,
                                  String plateNumber,
                                  String name,
                                  String bodyType,
                                  String imagePath,
                                  String description) {
        carRepository.findByPlateNumber(plateNumber).ifPresentOrElse(car -> {
            if (car.getCity() == null || car.getCity().isBlank()) {
                car.setCity(city);
                carRepository.save(car);
            }
        }, () -> {
            Car car = new Car();
            car.setName(name);
            car.setType(type);
            car.setCity(city);
            car.setBodyType(bodyType);
            car.setPlateNumber(plateNumber);
            car.setImagePath(imagePath);
            car.setDescription(description);
            car.setActive(true);
            carRepository.save(car);
        });
    }

    private String inferCityByPlate(String plateNumber) {
        if (plateNumber == null) {
            return "Омск";
        }
        String digits = plateNumber.replaceAll("\\D", "");
        if (digits.endsWith("55") || digits.endsWith("155")) {
            return "Омск";
        }
        if (digits.endsWith("54") || digits.endsWith("154")) {
            return "Новосибирск";
        }
        return "Омск";
    }
}
