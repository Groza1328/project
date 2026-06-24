package ru.sibmobile.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.CarType;
import ru.sibmobile.model.Employee;
import ru.sibmobile.service.CustomUserDetailsService;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.EmployeeRepository;
import ru.sibmobile.repository.UserRepository;
import ru.sibmobile.repository.CarRepository;
import ru.sibmobile.service.AdminService;
import ru.sibmobile.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CarRepository carRepository;
    private final ru.sibmobile.repository.OrderRepository orderRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminService adminService,
                           UserRepository userRepository,
                           EmployeeRepository employeeRepository,
                           CarRepository carRepository,
                           ru.sibmobile.repository.OrderRepository orderRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.carRepository = carRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("users", adminService.getAllUsersForAdmin());
        model.addAttribute("tariffTypes", TariffType.values());
        return "admin";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("employeeRoles", List.of("Сотрудник", "Менеджер", "Механик", "Администратор"));
        return "admin-employees";
    }

    @GetMapping("/cars")
    public String cars(Model model) {
        model.addAttribute("cars", carRepository.findAll());
        model.addAttribute("bodyTypes", List.of("Седан", "Кроссовер", "Хэтчбек", "Универсал", "Минивэн", "Внедорожник"));
        return "admin-cars";
    }

    @PostMapping("/cars/create")
    public String createCar(@RequestParam String name,
                            @RequestParam String bodyType,
                            @RequestParam String plateNumber,
                            @RequestParam(defaultValue = "Омск") String city,
                            @RequestParam(required = false) MultipartFile imageFile,
                            @RequestParam(required = false) String description,
                            @RequestParam(defaultValue = "true") boolean active) {
        String plate = plateNumber == null ? "" : plateNumber.trim();
        if (name == null || name.isBlank() || plate.isBlank() || bodyType == null || bodyType.isBlank()) {
            return "redirect:/admin/cars?error=invalid";
        }
        if (carRepository.existsByPlateNumber(plate)) {
            return "redirect:/admin/cars?error=exists";
        }
        Car car = new Car();
        car.setName(name.trim());
        car.setType(mapBodyTypeToCarType(bodyType));
        car.setBodyType(bodyType.trim());
        car.setPlateNumber(plate);
        car.setCity(city == null || city.isBlank() ? "Омск" : city.trim());
        String savedImage = saveCarImage(imageFile);
        car.setImagePath(savedImage != null ? savedImage : "/images/Auto1.jpg");
        car.setDescription(description == null ? null : description.trim());
        car.setActive(active);
        carRepository.save(car);
        return "redirect:/admin/cars?created=" + plate;
    }

    @PostMapping("/cars/update")
    public String updateCar(@RequestParam Long carId,
                            @RequestParam String name,
                            @RequestParam String bodyType,
                            @RequestParam String plateNumber,
                            @RequestParam(defaultValue = "Омск") String city,
                            @RequestParam(required = false) MultipartFile imageFile,
                            @RequestParam(required = false) String description,
                            @RequestParam(defaultValue = "false") boolean active) {
        Optional<Car> opt = carRepository.findById(carId);
        if (opt.isEmpty()) {
            return "redirect:/admin/cars?error=notfound";
        }
        String plate = plateNumber == null ? "" : plateNumber.trim();
        if (name == null || name.isBlank() || plate.isBlank() || bodyType == null || bodyType.isBlank()) {
            return "redirect:/admin/cars?error=invalid";
        }
        Car car = opt.get();
        boolean plateTaken = carRepository.findAll().stream()
            .anyMatch(c -> !c.getId().equals(carId) && plate.equalsIgnoreCase(c.getPlateNumber()));
        if (plateTaken) {
            return "redirect:/admin/cars?error=exists";
        }
        car.setName(name.trim());
        car.setType(mapBodyTypeToCarType(bodyType));
        car.setBodyType(bodyType.trim());
        car.setPlateNumber(plate);
        car.setCity(city == null || city.isBlank() ? "Омск" : city.trim());
        String savedImage = saveCarImage(imageFile);
        if (savedImage != null) {
            car.setImagePath(savedImage);
        } else if (car.getImagePath() == null || car.getImagePath().isBlank()) {
            car.setImagePath("/images/Auto1.jpg");
        }
        car.setDescription(description == null ? null : description.trim());
        car.setActive(active);
        carRepository.save(car);
        return "redirect:/admin/cars?updated=" + plate;
    }

    @PostMapping("/cars/delete")
    public String deleteCar(@RequestParam Long carId) {
        Optional<Car> opt = carRepository.findById(carId);
        if (opt.isEmpty()) {
            return "redirect:/admin/cars?error=notfound";
        }
        String plate = opt.get().getPlateNumber();
        carRepository.deleteById(carId);
        return "redirect:/admin/cars?deleted=" + plate;
    }

    private CarType mapBodyTypeToCarType(String bodyType) {
        if (bodyType == null) {
            return CarType.SOLARIS;
        }
        String normalized = bodyType.trim().toLowerCase();
        if (normalized.contains("крос")) {
            return CarType.NISSAN_QASHQAI;
        }
        if (normalized.contains("внедорож") || normalized.contains("suv")) {
            return CarType.GEELY_XINGYUE;
        }
        if (normalized.contains("минивэн")) {
            return CarType.GEELY_XINGYUE;
        }
        return CarType.SOLARIS;
    }

    private String saveCarImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String originalName = imageFile.getOriginalFilename();
        String ext = ".jpg";
        if (originalName != null) {
            int idx = originalName.lastIndexOf('.');
            if (idx >= 0 && idx < originalName.length() - 1) {
                ext = originalName.substring(idx).toLowerCase();
            }
        }
        if (!ext.matches("\\.(jpg|jpeg|png|webp)$")) {
            return null;
        }
        try {
            Path uploadDir = Paths.get("src", "main", "resources", "static", "images", "uploads");
            Files.createDirectories(uploadDir);
            String fileName = "car_" + UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(fileName);
            Files.copy(imageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/images/uploads/" + fileName;
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/employees/create")
    public String createEmployee(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String fullName,
                                 @RequestParam String position,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(defaultValue = "EMPLOYEE") String role,
                                 @RequestParam(defaultValue = "true") boolean active) {
        String login = username == null ? "" : username.trim();
        String mail = email == null ? "" : email.trim();
        if (login.isEmpty() || password == null || password.length() < 6 || fullName == null || fullName.isBlank() || mail.isEmpty()) {
            return "redirect:/admin/employees?error=invalid";
        }
        if (employeeRepository.existsByUsername(login) || employeeRepository.existsByEmail(mail)) {
            return "redirect:/admin/employees?error=exists";
        }

        Employee employee = new Employee();
        employee.setUsername(login);
        employee.setPasswordHash(passwordEncoder.encode(password));
        employee.setFullName(fullName.trim());
        employee.setPosition(position == null ? "" : position.trim());
        employee.setEmail(mail);
        employee.setPhone(phone == null ? null : phone.trim());
        employee.setRole(role == null || role.isBlank() ? "Сотрудник" : role.trim());
        employee.setActive(active);
        employee.setHiredAt(LocalDateTime.now());
        employeeRepository.save(employee);

        return "redirect:/admin/employees?created=" + login;
    }

    @PostMapping("/employees/update")
    public String updateEmployee(@RequestParam Long employeeId,
                                 @RequestParam String username,
                                 @RequestParam(required = false) String password,
                                 @RequestParam String fullName,
                                 @RequestParam String position,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(defaultValue = "EMPLOYEE") String role,
                                 @RequestParam(defaultValue = "false") boolean active) {
        Optional<Employee> opt = employeeRepository.findById(employeeId);
        if (opt.isEmpty()) {
            return "redirect:/admin/employees?error=notfound";
        }
        Employee employee = opt.get();

        String login = username == null ? "" : username.trim();
        String mail = email == null ? "" : email.trim();
        if (login.isEmpty() || fullName == null || fullName.isBlank() || mail.isEmpty()) {
            return "redirect:/admin/employees?error=invalid";
        }

        boolean usernameTaken = employeeRepository.findByUsername(login)
            .filter(e -> !e.getId().equals(employeeId))
            .isPresent();
        boolean emailTaken = employeeRepository.findByEmail(mail)
            .filter(e -> !e.getId().equals(employeeId))
            .isPresent();
        if (usernameTaken || emailTaken) {
            return "redirect:/admin/employees?error=exists";
        }

        employee.setUsername(login);
        employee.setFullName(fullName.trim());
        employee.setPosition(position == null ? "" : position.trim());
        employee.setEmail(mail);
        employee.setPhone(phone == null ? null : phone.trim());
        employee.setRole(role == null || role.isBlank() ? "Сотрудник" : role.trim());
        employee.setActive(active);
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                return "redirect:/admin/employees?error=badpass";
            }
            employee.setPasswordHash(passwordEncoder.encode(password));
        }
        employeeRepository.save(employee);
        return "redirect:/admin/employees?updated=" + login;
    }

    @PostMapping("/employees/delete")
    public String deleteEmployee(@RequestParam Long employeeId) {
        Optional<Employee> opt = employeeRepository.findById(employeeId);
        if (opt.isEmpty()) {
            return "redirect:/admin/employees?error=notfound";
        }
        String username = opt.get().getUsername();
        employeeRepository.deleteById(employeeId);
        return "redirect:/admin/employees?deleted=" + username;
    }

    @PostMapping("/block")
    public String blockUser(@RequestParam Long userId, @RequestParam(required = false) String reason) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "redirect:/admin?error=user";
        User u = opt.get();
        u.setBlocked(true);
        u.setRestrictedUntil(null);
        u.setRestrictionReason(reason != null ? reason.trim() : null);
        userRepository.save(u);
        emailService.sendBlockNotification(u.getEmail(), u.getRestrictionReason());
        return "redirect:/admin?blocked=" + u.getUsername();
    }

    @PostMapping("/restrict")
    public String restrictUser(@RequestParam Long userId,
                               @RequestParam(required = false) String reason,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime restrictedUntil) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "redirect:/admin?error=user";
        User u = opt.get();
        u.setBlocked(false);
        u.setRestrictedUntil(restrictedUntil);
        u.setRestrictionReason(reason != null ? reason.trim() : null);
        userRepository.save(u);
        emailService.sendRestrictNotification(u.getEmail(), u.getRestrictionReason(), restrictedUntil);
        return "redirect:/admin?restricted=" + u.getUsername();
    }

    @PostMapping("/unblock")
    public String unblockUser(@RequestParam Long userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "redirect:/admin?error=user";
        User u = opt.get();
        u.setBlocked(false);
        u.setRestrictedUntil(null);
        u.setRestrictionReason(null);
        userRepository.save(u);
        return "redirect:/admin?unblocked=" + u.getUsername();
    }

    @PostMapping("/tariff")
    public String changeTariff(@RequestParam Long userId, @RequestParam TariffType tariff) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "redirect:/admin?error=user";
        User u = opt.get();
        u.setTariff(tariff);
        u.setTariffSince(tariff == TariffType.STANDARD ? null : LocalDateTime.now());
        userRepository.save(u);
        return "redirect:/admin?tariff=" + u.getUsername();
    }

    @PostMapping("/delete")
    @Transactional
    public String deleteUser(@RequestParam Long userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "redirect:/admin?error=user";
        User u = opt.get();
        if (CustomUserDetailsService.ADMIN_USERNAME.equals(u.getUsername())) {
            return "redirect:/admin?admin";
        }
        orderRepository.deleteByUserId(userId);
        userRepository.delete(u);
        return "redirect:/admin?deleted=" + u.getUsername();
    }
}
