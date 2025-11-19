package app.maintenance.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.exception.DomainException;
import app.maintenance.model.Maintenance;
import app.maintenance.repository.MaintenanceRepository;

import java.math.BigDecimal;
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

        if (maintenance.getCar() == null || maintenance.getType() == null){

            throw DomainException.blankEntitiesForMaintenance();
        }

        maintenanceRepository.save(maintenance);
    }

    public Maintenance getForUser(UUID maintenanceId, User user) {
        return maintenanceRepository.findById(maintenanceId)
                .filter(m -> m.getCar() != null
                        && m.getCar().getUser() != null
                        && m.getCar().getUser().getId().equals(user.getId()))
                .orElseThrow(NoSuchElementException::new);
    }

    public int countForCar(UUID carId) {
        return maintenanceRepository.countByCarId(carId);
    }

    @Transactional
    public void update(UUID maintenanceId, User user, Maintenance updated) {
        Maintenance existing = getForUser(maintenanceId, user);

        if (updated.getCar() == null || updated.getType() == null) {
            throw DomainException.blankEntitiesForMaintenance();
        }

        if (updated.getCar().getId() != null) {
            Car car = cars.findByIdAndUserId(updated.getCar().getId(), user.getId())
                    .orElseThrow(NoSuchElementException::new);
            existing.setCar(car);
        }

        existing.setDate(updated.getDate());
        existing.setType(updated.getType());
        existing.setMileage(updated.getMileage());
        existing.setDescription(updated.getDescription());
        existing.setCost(updated.getCost());
        existing.setNextDueDate(updated.getNextDueDate());
    }

    public List<Maintenance> upcomingNext30Days(User user) {
        LocalDate today = LocalDate.now();
        LocalDate within30Days = today.plusDays(30);
        return upcomingForUser(user).stream()
                .filter(m -> m.getNextDueDate() != null)
                .filter(m -> !m.getNextDueDate().isBefore(today) && !m.getNextDueDate().isAfter(within30Days))
                .toList();
    }

    public List<Maintenance> getRecentMaintenances(User user, int limit) {
        return listForUser(user).stream()
                .limit(limit)
                .toList();
    }

    public BigDecimal getMonthlyCost(User user) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        return listForUser(user).stream()
                .filter(m -> m.getDate() != null)
                .filter(m -> {
                    LocalDate mDate = m.getDate();
                    return mDate.getYear() == currentYear && mDate.getMonthValue() == currentMonth;
                })
                .filter(m -> m.getCost() != null)
                .map(Maintenance::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSpent(List<Maintenance> maintenances) {
        return maintenances.stream()
                .filter(m -> m.getCost() != null)
                .map(Maintenance::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getThisMonthCount(List<Maintenance> maintenances) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        return maintenances.stream()
                .filter(m -> m.getDate() != null && !m.getDate().isBefore(firstDayOfMonth))
                .count();
    }

    public long getServiceReadyCount(User user) {
        return upcomingNext30Days(user).stream()
                .map(m -> m.getCar() != null ? m.getCar().getId() : null)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
    }


    public List<Maintenance> getAll() {

        return maintenanceRepository.findAll();
    }
}
