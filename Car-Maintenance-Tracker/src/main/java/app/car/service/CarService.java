package app.car.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.exception.CarCreateException;
import app.exception.CarUpdateException;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CarService {
    private final CarRepository carRepository;

    public CarService(CarRepository cars) {
        this.carRepository = cars;
    }


    @Cacheable(value = "carsByUser", key = "#user.id")
    public List<Car> getCarsForUser(User user) {
        return carRepository.findAllByUserIdAndDeletedFalse(user.getId());
    }

    public int getLatestAdditions(List<Car> cars) {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        int count = 0;

        for (Car car : cars) {
            if (car.getJoinedAt().isAfter(lastMonth)) {
                count++;
            }
        }

        return count;
    }

    @CacheEvict(value = "carsByUser", key = "#user.id")
    public void createCar(Car car, User user) {
        if (car.getBrand().isBlank() || car.getModel().isBlank() || car.getVin().isBlank()){
            throw CarCreateException.requiredFieldsForCar(car);
        }

        car.setUser(user);
        carRepository.save(car);
        log.info("Successfully created car with ID: {} for user: {}", car.getId(), user.getId());
    }

    public List<Car> filterCars(User user, String brand, String model, String search) {

        List<Car> cars = getCarsForUser(user);

        return cars.stream()
                .filter(car -> brand == null || brand.isBlank() ||
                        car.getBrand().equalsIgnoreCase(brand))

                .filter(car -> model == null || model.isBlank() ||
                        car.getModel().equalsIgnoreCase(model))

                .filter(car -> {
                    if (search == null || search.isBlank()) return true;

                    String term = search.toLowerCase();

                    boolean matchesYear = String.valueOf(car.getYear()).contains(term);
                    boolean matchesVin = car.getVin().toLowerCase().contains(term);
                    boolean matchesModel = car.getModel().toLowerCase().contains(term);

                    return matchesYear || matchesVin || matchesModel;
                })

                .toList();
    }

    @Transactional
    public void deleteCar(UUID carId, User user) {
        Car car = getCarForUser(carId, user);
        car.setDeleted(true);
        carRepository.save(car);
        log.info("Successfully deleted car with ID: {} for user: {}", carId, user.getId());
    }

    public Car getCarForUser(UUID carId, User user) {
        return carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId())
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public void updateCar(UUID carId, User user, Car updatedCar) {
        Car existing = getCarForUser(carId, user);

        if (updatedCar.getBrand().isBlank() || updatedCar.getModel().isBlank() || updatedCar.getVin().isBlank()) {
            throw CarUpdateException.requiredFieldsForCar(carId);
        }

        existing.setBrand(updatedCar.getBrand());
        existing.setModel(updatedCar.getModel());
        existing.setYear(updatedCar.getYear());
        existing.setVin(updatedCar.getVin());
        log.info("Successfully updated car with ID: {} for user: {}", carId, user.getId());
    }

    public Map<String, Set<String>> getBrandModelsMap(List<Car> cars) {
        return cars.stream()
                .collect(Collectors.groupingBy(
                        Car::getBrand,
                        Collectors.mapping(Car::getModel, Collectors.toSet())
                ));
    }

    public Map<UUID, Integer> getMaintenanceCountsMap(List<Car> cars, MaintenanceService maintenanceService) {
        return cars.stream()
                .collect(Collectors.toMap(
                        Car::getId,
                        car -> maintenanceService.countForCar(car.getId())
                ));
    }
    public List<Car> getAll() {

        return carRepository.findAll();
    }

    @CacheEvict(value = "carsByUser", allEntries = true)
    public void clearAllCarCache() {
        log.info("Car cache cleared by scheduler");
    }
}
