package app.car.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarService {
    private final CarRepository cars;
    private final UserRepository users;

    public CarService(CarRepository cars, UserRepository users) {
        this.cars = cars;
        this.users = users;
    }

    public List<Car> listForUser(UUID userId) {
        return cars.findAllByUserId(userId);
    }

    public Optional<Car> get(UUID userId, UUID carId) {
        return cars.findByIdAndUserId(carId, userId);
    }

    @Transactional
    public void delete(UUID userId, UUID carId) {
        cars.deleteByIdAndUserId(carId, userId);
    }
}