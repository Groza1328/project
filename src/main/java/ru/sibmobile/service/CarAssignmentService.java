package ru.sibmobile.service;

import org.springframework.stereotype.Service;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.CarType;
import ru.sibmobile.repository.CarRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarAssignmentService {

    private final CarRepository carRepository;

    public CarAssignmentService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Optional<Car> assignAvailableCar(CarType type, String city,
                                            LocalDateTime startDateTime,
                                            LocalDateTime endDateTime) {
        if (type == null || city == null || city.isBlank()
                || startDateTime == null || endDateTime == null) {
            return Optional.empty();
        }

        List<Car> candidates = carRepository.findByTypeAndCityAndActiveTrueOrderByIdAsc(type, city.trim());
        for (Car car : candidates) {
            if (!carRepository.hasOverlappingOrder(car.getId(), startDateTime, endDateTime)) {
                return Optional.of(car);
            }
        }
        return Optional.empty();
    }
}
