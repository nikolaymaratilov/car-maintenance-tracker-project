package app.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MaintenanceInfo {
    private UUID maintenanceId;
    private UUID carId;
    private String carBrand;
    private String carModel;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String type;
    private String description;
    private int mileage;
    private BigDecimal cost;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueDate;

    public MaintenanceInfo() {
    }

    public MaintenanceInfo(UUID maintenanceId, UUID carId, String carBrand, String carModel,
                          LocalDate date, String type, String description, int mileage,
                          BigDecimal cost, LocalDate nextDueDate) {
        this.maintenanceId = maintenanceId;
        this.carId = carId;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.date = date;
        this.type = type;
        this.description = description;
        this.mileage = mileage;
        this.cost = cost;
        this.nextDueDate = nextDueDate;
    }

    public UUID getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(UUID maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
}

