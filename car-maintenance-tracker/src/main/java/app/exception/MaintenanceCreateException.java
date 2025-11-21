package app.exception;

public class MaintenanceCreateException extends RuntimeException{

    public MaintenanceCreateException(String message){
        super(message);
    }
    public static MaintenanceCreateException blankEntitiesForMaintenance(){

        return new MaintenanceCreateException("Car and type are required fields!");
    }
}
