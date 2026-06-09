package ru.sibmobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sibmobile.model.Car;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByActiveTrueOrderByNameAsc();

    boolean existsByPlateNumber(String plateNumber);
}

