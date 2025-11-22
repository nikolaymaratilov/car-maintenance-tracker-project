package app.exception;

import app.car.model.Car;

public class CarCreateException extends RuntimeException {

    private final Car car;

    public CarCreateException(String message, Car car) {
        super(message);
        this.car = car;
    }

    public static CarCreateException requiredFieldsForCar(Car car){

        return new CarCreateException("Brand,model and vin are required fields!",car);
    }

    public Car getCar() {
        return car;
    }
}
