package ru.sibmobile.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.sibmobile.dto.RegisterForm;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.service.TariffService;
import ru.sibmobile.service.TariffService.CarPrice;
import ru.sibmobile.repository.CarRepository;
import ru.sibmobile.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;
    private final TariffService tariffService;
    private final CarRepository carRepository;

    public AuthController(UserService userService, TariffService tariffService, CarRepository carRepository) {
        this.userService = userService;
        this.tariffService = tariffService;
        this.carRepository = carRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterForm form,
                           BindingResult result,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.isUsernameTaken(form.getUsername())) {
            result.rejectValue("username", "error.username", "Этот логин уже занят");
            return "register";
        }

        boolean emailSent;
        if (userService.isEmailTaken(form.getEmail())) {
            result.rejectValue("email", "error.email", "Этот email уже зарегистрирован");
            return "register";
        }

        if (userService.hasPendingVerification(form.getEmail())) {
            emailSent = userService.resumePendingRegistration(
                form.getUsername(), form.getEmail(), form.getPassword());
        } else {
            emailSent = userService.registerUser(
                form.getUsername(), form.getEmail(), form.getPassword());
        }

        String mail = form.getEmail().trim().toLowerCase();
        session.setAttribute("verifyEmail", mail);
        session.setAttribute("verifyPassword", form.getPassword());
        model.addAttribute("email", mail);
        model.addAttribute("justRegistered", true);
        if (!emailSent) {
            model.addAttribute("emailWarning",
                "Не удалось отправить письмо. Проверьте адрес или нажмите «Отправить код повторно».");
        }
        return "verify";
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session,
                            @RequestParam(required = false) String email,
                            Model model) {
        String sessionEmail = (String) session.getAttribute("verifyEmail");
        if (sessionEmail == null && email != null && !email.isBlank()) {
            sessionEmail = email.trim().toLowerCase();
            session.setAttribute("verifyEmail", sessionEmail);
        }
        if (sessionEmail == null) {
            return "redirect:/register";
        }
        model.addAttribute("email", sessionEmail);
        return "verify";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String code,
                         HttpSession session,
                         Model model,
                         HttpServletRequest request) {
        String email = (String) session.getAttribute("verifyEmail");
        String password = (String) session.getAttribute("verifyPassword");
        if (email == null) {
            return "redirect:/register";
        }

        if (userService.verifyUser(email, code)) {
            session.removeAttribute("verifyEmail");
            session.removeAttribute("verifyPassword");
            String username = userService.getUsernameByEmail(email);
            if (username != null && password != null) {
                try {
                    request.login(username, password);
                    return "redirect:/home";
                } catch (Exception ignored) {
                }
            }
            return "redirect:/login?verified";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Неверный код подтверждения");
        return "verify";
    }

    @PostMapping("/verify/resend")
    public String resendCode(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("verifyEmail");
        if (email == null) {
            return "redirect:/register";
        }
        if (userService.resendVerificationCode(email)) {
            redirectAttributes.addFlashAttribute("resendSuccess", "Код отправлен повторно на вашу почту.");
        } else {
            redirectAttributes.addFlashAttribute("emailWarning",
                "Не удалось отправить письмо. Проверьте настройки SMTP или попробуйте позже.");
        }
        return "redirect:/verify";
    }

    @GetMapping("/verify/change-email")
    public String changeEmail(HttpSession session) {
        session.removeAttribute("verifyEmail");
        session.removeAttribute("verifyPassword");
        return "redirect:/register";
    }

    @GetMapping("/verify/cancel")
    public String cancelVerify(HttpSession session) {
        session.removeAttribute("verifyEmail");
        session.removeAttribute("verifyPassword");
        return "redirect:/register";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/fleet")
    public String fleet(Model model) {
        TariffType tariff = tariffService.getCurrentTariff();
        model.addAttribute("tariff", tariff);
        model.addAttribute("canFreeParking", tariffService.canFreeParkingAnywhere(tariff));

        java.util.List<Car> cars = carRepository.findByActiveTrueOrderByNameAsc();
        java.util.List<java.util.Map<String, Object>> carViews = new java.util.ArrayList<>();

        for (Car car : cars) {
            CarPrice price = tariffService.calculatePrice(tariff, car.getType());
            java.util.Map<String, Object> view = new java.util.HashMap<>();
            view.put("id", car.getId());
            view.put("name", car.getName());
            view.put("type", car.getType());
            view.put("imagePath", car.getImagePath());
            view.put("description", car.getDescription());
            view.put("preBase", (int) price.getBasePrepayment());
            view.put("pre", (int) price.getPrepayment());
            view.put("kmBase", price.getBasePerKm());
            view.put("km", price.getPerKm());
            carViews.add(view);
        }

        model.addAttribute("cars", carViews);
        return "fleet";
    }

    @GetMapping("/tariffs")
    public String tariffs(Model model) {
        TariffType tariff = tariffService.getCurrentTariff();
        model.addAttribute("tariff", tariff);
        model.addAttribute("canFreeParking", tariffService.canFreeParkingAnywhere(tariff));
        return "tariffs";
    }

    @PostMapping("/tariffs/select")
    public String selectTariff(@RequestParam TariffType type) {
        tariffService.changeTariff(type);
        return "redirect:/tariffs";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                HttpSession session,
                                Model model) {
        if (email == null || email.isBlank()) {
            model.addAttribute("error", "Введите email");
            return "forgot-password";
        }

        String mail = email.trim().toLowerCase();
        if (!userService.isRegisteredEmail(mail)) {
            model.addAttribute("error",
                "Аккаунт с таким email не найден. Проверьте адрес или зарегистрируйтесь.");
            model.addAttribute("submittedEmail", mail);
            return "forgot-password";
        }

        boolean sent = userService.startPasswordReset(mail);
        session.setAttribute("resetEmail", mail);
        model.addAttribute("email", mail);
        model.addAttribute("codeSent", true);
        if (!sent) {
            model.addAttribute("emailWarning",
                "Код создан, но письмо не отправилось. Нажмите «Отправить код повторно» или проверьте SMTP.");
        }
        return "reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session,
                                    @RequestParam(required = false) String email,
                                    Model model) {
        String sessionEmail = (String) session.getAttribute("resetEmail");
        if (sessionEmail == null && email != null && !email.isBlank()) {
            sessionEmail = email.trim().toLowerCase();
            session.setAttribute("resetEmail", sessionEmail);
        }
        if (sessionEmail == null) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", sessionEmail);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String code,
                                @RequestParam("password") String newPassword,
                                @RequestParam("passwordConfirm") String passwordConfirm,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Пароль должен быть минимум 6 символов");
            return "reset-password";
        }

        if (!newPassword.equals(passwordConfirm)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Пароли не совпадают");
            return "reset-password";
        }

        if (userService.resetPassword(email, code, newPassword)) {
            session.removeAttribute("resetEmail");
            return "redirect:/login?resetSuccess";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Неверный код. Проверьте письмо или запросите код повторно.");
        return "reset-password";
    }

    @PostMapping("/reset-password/resend")
    public String resendResetCode(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }
        if (userService.resendPasswordResetCode(email)) {
            redirectAttributes.addFlashAttribute("resendSuccess", "Новый код отправлен на вашу почту.");
        } else {
            redirectAttributes.addFlashAttribute("emailWarning",
                "Не удалось отправить письмо. Проверьте email или настройки почтового сервера.");
        }
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/reset-password?email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
    }
}
