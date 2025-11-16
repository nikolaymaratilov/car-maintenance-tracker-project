package app.maintenance.repository;

import app.maintenance.model.Maintenance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    List<Maintenance> findAllByCarIdOrderByDateDesc(UUID carId);
    List<Maintenance> findAllByCarUserIdOrderByDateDesc(UUID userId);
    Optional<Maintenance> findByIdAndCarId(UUID id, UUID carId);
    void deleteByIdAndCarId(UUID id, UUID carId);
}