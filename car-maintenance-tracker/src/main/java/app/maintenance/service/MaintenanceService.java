package app.maintenance.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.exception.DomainException;
import app.maintenance.model.Maintenance;
import app.maintenance.repository.MaintenanceRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import app.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final CarRepository cars;

    public MaintenanceService(MaintenanceRepository maintenances, CarRepository cars) {
        this.maintenanceRepository = maintenances;
        this.cars = cars;
    }

    @Transactional
    public Maintenance add(UUID carId, Maintenance maintenance) {
        Car car = cars.findById(carId).orElseThrow(NoSuchElementException::new);
        maintenance.setCar(car);
        return maintenanceRepository.save(maintenance);
    }

    public List<Maintenance> listForCar(UUID carId) {
        return maintenanceRepository.findAllByCarIdOrderByDateDesc(carId);
    }

    public List<Maintenance> listForUser(User user) {
        return maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId());
    }

    public List<Maintenance> filterForUser(User user,
                                           UUID carId,
                                           app.maintenance.model.MaintenanceType type,
                                           String search) {

        List<Maintenance> maintenances = listForUser(user);

        return maintenances.stream()
                .filter(m -> carId == null || (m.getCar() != null && carId.equals(m.getCar().getId())))
                .filter(m -> type == null || m.getType() == type)
                .filter(m -> {
                    if (search == null || search.isBlank()) {
                        return true;
                    }
                    String term = search.toLowerCase();
                    String description = m.getDescription() == null ? "" : m.getDescription().toLowerCase();
                    String carText = m.getCar() == null
                            ? ""
                            : (m.getCar().getBrand() + " " + m.getCar().getModel()).toLowerCase();
                    return description.contains(term) || carText.contains(term);
                })
                .toList();
    }

    public List<Maintenance> upcomingForUser(User user) {
        LocalDate today = LocalDate.now();

        return listForUser(user).stream()
                .filter(m -> m.getNextDueDate() != null && !m.getNextDueDate().isBefore(today))
                .sorted(Comparator.comparing(Maintenance::getNextDueDate))
                .toList();
    }

    public Optional<Maintenance> getById(UUID maintenanceId) {
        return maintenanceRepository.findById(maintenanceId);
    }

    public Optional<Maintenance> get(UUID carId, UUID maintenanceId) {
        return maintenanceRepository.findByIdAndCarId(maintenanceId, carId);
    }

    @Transactional
    public void delete(UUID carId, UUID maintenanceId) {
        maintenanceRepository.deleteByIdAndCarId(maintenanceId, carId);
    }

    public void createMaintenance(Maintenance maintenance) {

        if (maintenance.getCar().toString().isBlank() || maintenance.getType().toString().isBlank()){

            throw DomainException.blankEntitiesForMaintenance();
        }

        maintenanceRepository.save(maintenance);
    }
}