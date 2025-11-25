package app.avatarPdf.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserProfileData {
    private UserInfo userInfo;
    private List<CarInfo> cars;
    private List<MaintenanceInfo> maintenances;
    private int totalCars;
    private int totalMaintenances;
    private BigDecimal totalMaintenanceCost;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    public UserProfileData() {
    }

    public UserProfileData(UserInfo userInfo, List<CarInfo> cars, List<MaintenanceInfo> maintenances,
                          int totalCars, int totalMaintenances, BigDecimal totalMaintenanceCost,
                          LocalDateTime generatedAt) {
        this.userInfo = userInfo;
        this.cars = cars;
        this.maintenances = maintenances;
        this.totalCars = totalCars;
        this.totalMaintenances = totalMaintenances;
        this.totalMaintenanceCost = totalMaintenanceCost;
        this.generatedAt = generatedAt;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<CarInfo> getCars() {
        return cars;
    }

    public void setCars(List<CarInfo> cars) {
        this.cars = cars;
    }

    public List<MaintenanceInfo> getMaintenances() {
        return maintenances;
    }

    public void setMaintenances(List<MaintenanceInfo> maintenances) {
        this.maintenances = maintenances;
    }

    public int getTotalCars() {
        return totalCars;
    }

    public void setTotalCars(int totalCars) {
        this.totalCars = totalCars;
    }

    public int getTotalMaintenances() {
        return totalMaintenances;
    }

    public void setTotalMaintenances(int totalMaintenances) {
        this.totalMaintenances = totalMaintenances;
    }

    public BigDecimal getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(BigDecimal totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}

