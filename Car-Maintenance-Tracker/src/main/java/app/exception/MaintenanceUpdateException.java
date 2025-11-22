package app.exception;

import java.util.UUID;

public class MaintenanceUpdateException extends RuntimeException{

    private final UUID maintenanceId;

    public MaintenanceUpdateException(String message, UUID maintenanceId){
        super(message);
        this.maintenanceId = maintenanceId;
    }
    public static MaintenanceUpdateException blankEntitiesForMaintenance(UUID maintenanceId){

        return new MaintenanceUpdateException("Car and type are required fields!", maintenanceId);
    }

    public UUID getMaintenanceId() {
        return maintenanceId;
    }
}
