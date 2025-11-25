package app.avatarPdf;

import app.avatarPdf.dto.AvatarPdfResponse;
import app.avatarPdf.dto.CarInfo;
import app.avatarPdf.dto.MaintenanceInfo;
import app.avatarPdf.dto.UserInfo;
import app.avatarPdf.dto.UserProfileData;
import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;
import app.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvatarPdfService {

    private final AvatarPdfClient client;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CarService carService;
    private final MaintenanceService maintenanceService;

    public byte[] generatePdf(MultipartFile file, String displayName) {
        return client.createPdf(file, displayName);
    }

    public byte[] generatePdfWithProfile(MultipartFile file, String displayName, UserProfileData userProfileData) {
        try {
            String userProfileDataJson = objectMapper.writeValueAsString(userProfileData);
            return client.createPdfWithProfile(file, displayName, userProfileDataJson);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user profile data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF with profile: " + e.getMessage(), e);
        }
    }

    public AvatarPdfResponse getPdf(UUID id) {
        return client.getPdf(id);
    }

    public AvatarPdfResponse getLatestPdfForUser(UUID userId) {
        return client.getLatestPdfForUser(userId);
    }

    public void deleteLatestPdfForUser(UUID userId) {
        client.deleteLatestPdfForUserPost(userId);
    }

    public UserProfileData buildUserProfileData(UUID userId) {
        User user = userService.getById(userId);

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.getRole().name(),
                user.getCreatedOn(),
                user.getUpdatedOn()
        );

        List<Car> cars = carService.getCarsForUser(user);
        List<CarInfo> carInfos = cars.stream()
                .map(car -> new CarInfo(
                        car.getId(),
                        car.getBrand(),
                        car.getModel(),
                        car.getYear(),
                        car.getVin(),
                        car.getJoinedAt(),
                        maintenanceService.countForCar(car.getId())
                ))
                .collect(Collectors.toList());

        List<Maintenance> maintenances = maintenanceService.listForUser(user);
        List<MaintenanceInfo> maintenanceInfos = maintenances.stream()
                .map(m -> new MaintenanceInfo(
                        m.getId(),
                        m.getCar() != null ? m.getCar().getId() : null,
                        m.getCar() != null ? m.getCar().getBrand() : "N/A",
                        m.getCar() != null ? m.getCar().getModel() : "N/A",
                        m.getDate(),
                        m.getType() != null ? m.getType().name() : "N/A",
                        m.getDescription(),
                        m.getMileage(),
                        m.getCost(),
                        m.getNextDueDate()
                ))
                .collect(Collectors.toList());

        int totalCars = carInfos.size();
        int totalMaintenances = maintenanceInfos.size();
        BigDecimal totalMaintenanceCost = maintenances.stream()
                .filter(m -> m.getCost() != null)
                .map(Maintenance::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserProfileData(
                userInfo,
                carInfos,
                maintenanceInfos,
                totalCars,
                totalMaintenances,
                totalMaintenanceCost,
                LocalDateTime.now()
        );
    }

}
