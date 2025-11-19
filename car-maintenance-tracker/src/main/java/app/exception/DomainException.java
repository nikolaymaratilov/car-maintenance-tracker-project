package app.exception;

public class DomainException extends RuntimeException{


    public DomainException(String message){
        super(message);
    }

    public static DomainException invalidPassword(){

        return new DomainException("Incorrect current password.");
    }

    public static DomainException blankEntitiesForCars(){

        return new DomainException("Brand,model and vin are required fields!");
    }

    public static DomainException blankEntitiesForMaintenance(){

        return new DomainException("Car and type are required fields!");
    }
}
