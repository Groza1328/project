package ru.sibmobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sibmobile.model.Car;
import ru.sibmobile.model.CarType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByActiveTrueOrderByNameAsc();

    List<Car> findByTypeAndCityAndActiveTrueOrderByIdAsc(CarType type, String city);

    Optional<Car> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM Order o
            WHERE o.car.id = :carId
              AND o.startDateTime < :endDateTime
              AND o.endDateTime > :startDateTime
            """)
    boolean hasOverlappingOrder(@Param("carId") Long carId,
                                @Param("startDateTime") LocalDateTime startDateTime,
                                @Param("endDateTime") LocalDateTime endDateTime);
}
