package app.car.service;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.exception.DomainException;
import app.user.model.User;
import app.user.repository.UserRepository;

import java.time.YearMonth;
import java.util.List;


import org.springframework.stereotype.Service;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final UserRepository users;

    public CarService(CarRepository cars, UserRepository users) {
        this.carRepository = cars;
        this.users = users;
    }


    public List<Car> getCarsForUser(User user) {

        return carRepository.findAllByUserId(user.getId());
    }

    public int getLatestAdditions(List<Car> cars) {

        int count = 0;

        for (Car car:cars) {

            boolean isLastMonth = YearMonth.from(car.getJoinedAt()).equals(YearMonth.now().minusMonths(1));

            if (isLastMonth){

                count++;
            }

        }

        return count;
    }

    public void createCar(Car car, User user) {

        if (car.getBrand().isBlank() || car.getModel().isBlank() || car.getVin().isBlank()){
            throw DomainException.blankEntities();
        }

        car.setUser(user);
        carRepository.save(car);
    }
}