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
import ru.sibmobile.model.Car;
import ru.sibmobile.model.CarType;
import ru.sibmobile.model.Order;
import ru.sibmobile.model.TariffType;
import ru.sibmobile.model.User;
import ru.sibmobile.repository.OrderRepository;
import ru.sibmobile.repository.UserRepository;
import ru.sibmobile.service.CarAssignmentService;
import ru.sibmobile.service.EmailService;
import ru.sibmobile.service.TariffService;
import ru.sibmobile.service.TariffService.CarPrice;

import java.util.Optional;

@Controller
public class OrderController {

    private final TariffService tariffService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final CarAssignmentService carAssignmentService;

    public OrderController(TariffService tariffService,
                           UserRepository userRepository,
                           OrderRepository orderRepository,
                           EmailService emailService,
                           CarAssignmentService carAssignmentService) {
        this.tariffService = tariffService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.carAssignmentService = carAssignmentService;
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

        Optional<Car> assignedCar = Optional.empty();
        if (!bindingResult.hasErrors()) {
            assignedCar = carAssignmentService.assignAvailableCar(
                    form.getCarType(),
                    form.getCity(),
                    form.getStartDateTime(),
                    form.getEndDateTime());
            if (assignedCar.isEmpty()) {
                bindingResult.reject("car.unavailable",
                        "Нет свободных автомобилей этой модели в выбранном городе на указанное время. Выберите другие даты или город.");
            }
        }

        if (bindingResult.hasErrors()) {
            populateOrderModel(model, form, tariff, price);
            return "order";
        }

        Car car = assignedCar.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        User currentUser = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String login = auth.getName();
            currentUser = userRepository.findByUsername(login)
                    .or(() -> userRepository.findByEmail(login))
                    .orElse(null);
            if (currentUser != null) {
                email = currentUser.getEmail();
            }
        }

        if (email != null) {
            emailService.sendOrderConfirmationAsync(email, form, price, tariff, car);
            if (form.isSendReceipt()) {
                emailService.sendOrderReceiptAsync(email, form, price, tariff, car);
            }
        }

        if (currentUser != null) {
            Order order = new Order();
            order.setUser(currentUser);
            order.setCarType(form.getCarType());
            order.setCar(car);
            order.setStartDateTime(form.getStartDateTime());
            order.setEndDateTime(form.getEndDateTime());
            orderRepository.save(order);
        }

        model.addAttribute("carName", form.getCarType().getDisplayName());
        model.addAttribute("assignedPlate", car.getPlateNumber());
        model.addAttribute("assignedCity", car.getCity());
        model.addAttribute("price", price);
        model.addAttribute("tariff", tariff);
        model.addAttribute("email", email);
        model.addAttribute("receiptSent", form.isSendReceipt() && email != null);
        return "order-success";
    }

    private void populateOrderModel(Model model, OrderForm form, TariffType tariff, CarPrice price) {
        model.addAttribute("carType", form.getCarType());
        model.addAttribute("carName", form.getCarType().getDisplayName());
        model.addAttribute("tariff", tariff);
        model.addAttribute("price", price);
        model.addAttribute("canFreeParking", tariffService.canFreeParkingAnywhere(tariff));
    }
}
