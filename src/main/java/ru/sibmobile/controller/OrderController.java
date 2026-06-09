package ru.sibmobile.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sibmobile.dto.OrderForm;
import ru.sibmobile.model.CarType;
import ru.sibmobile.model.Order;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.OrderRepository;
import ru.sibmobile.repository.UserRepository;
import ru.sibmobile.service.TariffService;
import ru.sibmobile.service.TariffService.CarPrice;
import ru.sibmobile.service.EmailService;

@Controller
public class OrderController {

    private final TariffService tariffService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderController(TariffService tariffService, UserRepository userRepository,
                           OrderRepository orderRepository, EmailService emailService) {
        this.tariffService = tariffService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    @GetMapping("/order")
    public String orderForm(@RequestParam(value = "car", required = false) CarType carType, Model model) {
        if (carType == null) {
            carType = CarType.SOLARIS;
        }

        TariffType tariff = tariffService.getCurrentTariff();
        CarPrice price = tariffService.calculatePrice(tariff, carType);

        OrderForm form = new OrderForm();
        form.setCarType(carType);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String login = auth.getName();
            userRepository.findByUsername(login)
                    .or(() -> userRepository.findByEmail(login))
                    .ifPresent(user -> {
                        // по желанию можно префиллить имя/телефон, если они будут храниться в профиле
                    });
        }

        model.addAttribute("carType", carType);
        model.addAttribute("carName", carType.getDisplayName());
        model.addAttribute("tariff", tariff);
        model.addAttribute("price", price);
        model.addAttribute("orderForm", form);
        model.addAttribute("canFreeParking", tariffService.canFreeParkingAnywhere(tariff));
        return "order";
    }

    @PostMapping("/order")
    public String submitOrder(@Valid @ModelAttribute("orderForm") OrderForm form,
                              BindingResult bindingResult,
                              Model model) {
        TariffType tariff = tariffService.getCurrentTariff();
        CarPrice price = tariffService.calculatePrice(tariff, form.getCarType());

        if (form.getStartDateTime() != null && form.getEndDateTime() != null
                && !form.getEndDateTime().isAfter(form.getStartDateTime())) {
            bindingResult.rejectValue("endDateTime", "end.beforeStart", "Время окончания должно быть позже начала");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("carType", form.getCarType());
            model.addAttribute("carName", form.getCarType().getDisplayName());
            model.addAttribute("tariff", tariff);
            model.addAttribute("price", price);
            model.addAttribute("canFreeParking", tariffService.canFreeParkingAnywhere(tariff));
            return "order";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String login = auth.getName();
            email = userRepository.findByUsername(login)
                    .or(() -> userRepository.findByEmail(login))
                    .map(User::getEmail)
                    .orElse(null);
        }

        User currentUser = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String login = auth.getName();
            currentUser = userRepository.findByUsername(login)
                    .or(() -> userRepository.findByEmail(login))
                    .orElse(null);
        }
        if (email != null) {
            emailService.sendOrderConfirmation(email, form, price, tariff);
        }
        if (currentUser != null) {
            Order order = new Order();
            order.setUser(currentUser);
            order.setCarType(form.getCarType());
            order.setStartDateTime(form.getStartDateTime());
            order.setEndDateTime(form.getEndDateTime());
            orderRepository.save(order);
        }

        model.addAttribute("carName", form.getCarType().getDisplayName());
        model.addAttribute("price", price);
        model.addAttribute("tariff", tariff);
        model.addAttribute("email", email);
        return "order-success";
    }
}


