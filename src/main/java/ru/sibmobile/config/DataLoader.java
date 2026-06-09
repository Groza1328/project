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

            // Добавляем базовый автопарк (если таблица пуста)
            if (carRepository.count() == 0) {
                Car solaris = new Car();
                solaris.setName("Hyundai Solaris (2020–2023)");
                solaris.setType(CarType.SOLARIS);
                solaris.setBodyType("Седан");
                solaris.setPlateNumber("А000АА154");
                solaris.setImagePath("/images/Auto1.jpg");
                solaris.setDescription("Надёжный городской седан с экономичным расходом топлива.");
                solaris.setActive(true);
                carRepository.save(solaris);

                Car geely = new Car();
                geely.setName("Geely Xingyue S (Xingyue L)");
                geely.setType(CarType.GEELY_XINGYUE);
                geely.setBodyType("Кроссовер");
                geely.setPlateNumber("В111ВВ154");
                geely.setImagePath("/images/Auto2.jpg");
                geely.setDescription("Мощный кроссовер с продвинутыми ассистентами и полным приводом.");
                geely.setActive(true);
                carRepository.save(geely);

                Car qashqai = new Car();
                qashqai.setName("Nissan Qashqai II (J11)");
                qashqai.setType(CarType.NISSAN_QASHQAI);
                qashqai.setBodyType("Кроссовер");
                qashqai.setPlateNumber("С222СС154");
                qashqai.setImagePath("/images/Auto3.jpg");
                qashqai.setDescription("Популярный городской кроссовер, комфортный и практичный.");
                qashqai.setActive(true);
                carRepository.save(qashqai);
            }

            // Для существующих авто проставляем кузов, если поле пустое
            carRepository.findAll().forEach(car -> {
                if (car.getBodyType() == null || car.getBodyType().isBlank()) {
                    String bodyType = switch (car.getType()) {
                        case SOLARIS -> "Седан";
                        case GEELY_XINGYUE, NISSAN_QASHQAI -> "Кроссовер";
                    };
                    car.setBodyType(bodyType);
                    carRepository.save(car);
                }
            });
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
}
