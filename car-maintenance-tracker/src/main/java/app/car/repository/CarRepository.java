package app.car.repository;

import app.car.model.Car;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findAllByUserIdAndDeletedFalse(UUID userId);
    Optional<Car> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
    Optional<Car> findByIdAndUserId(UUID id, UUID userId);
}