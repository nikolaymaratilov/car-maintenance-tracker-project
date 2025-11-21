package app.exception;

import java.util.UUID;

public class CarUpdateException extends RuntimeException {
    private final UUID carId;

    public CarUpdateException(String message, UUID carId) {
        super(message);
        this.carId = carId;
    }

    public static CarUpdateException requiredFieldsForCar(UUID carId){

        return new CarUpdateException("Brand,model and vin are required fields!",carId);
    }

    public UUID getCarId() {
        return carId;
    }
}

