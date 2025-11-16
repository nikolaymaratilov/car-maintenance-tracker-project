package app.car.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.exception.DomainException;
import app.user.model.User;
import app.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarService {
    private final CarRepository carRepository;

    public CarService(CarRepository cars) {
        this.carRepository = cars;
    }


    public List<Car> getCarsForUser(User user) {

        return carRepository.findAllByUserId(user.getId());
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

    public void createCar(Car car, User user) {

        if (car.getBrand().isBlank() || car.getModel().isBlank() || car.getVin().isBlank()){
            throw DomainException.blankEntitiesForCars();
        }

        car.setUser(user);
        carRepository.save(car);
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
        carRepository.deleteByIdAndUserId(carId, user.getId());
    }

    public Car getCarForUser(UUID carId, User user) {
        return carRepository.findByIdAndUserId(carId, user.getId())
                .orElseThrow(NoSuchElementException::new);
    }

}