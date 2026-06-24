package ru.sibmobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sibmobile.model.Order;
import ru.sibmobile.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.car
            WHERE o.user = :user
            ORDER BY o.createdAt DESC
            """)
    List<Order> findByUserWithCarOrderByCreatedAtDesc(@Param("user") User user);

    /** Текущий активный прокат пользователя (сейчас между start и end) */
    Optional<Order> findFirstByUserAndStartDateTimeLessThanEqualAndEndDateTimeGreaterThanEqualOrderByStartDateTimeDesc(
            User user, LocalDateTime now, LocalDateTime now2);

    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
