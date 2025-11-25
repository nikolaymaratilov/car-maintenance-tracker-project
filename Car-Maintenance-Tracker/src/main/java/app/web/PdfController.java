package app.web;

import app.avatarPdf.AvatarPdfService;
import app.avatarPdf.dto.AvatarPdfResponse;
import app.avatarPdf.dto.CarInfo;
import app.avatarPdf.dto.MaintenanceInfo;
import app.avatarPdf.dto.UserInfo;
import app.avatarPdf.dto.UserProfileData;
import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.service.MaintenanceService;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pdf")
public class PdfController {

    private final AvatarPdfService avatarPdfService;
    private final UserService userService;
    private final CarService carService;
    private final MaintenanceService maintenanceService;

    public PdfController(
            AvatarPdfService avatarPdfService,
            UserService userService,
            CarService carService,
            MaintenanceService maintenanceService
    ) {
        this.avatarPdfService = avatarPdfService;
        this.userService = userService;
        this.carService = carService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/generate")
    public String generatePdfPage() {
        return "pdf-generate";
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateFullProfilePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @AuthenticationPrincipal UserData userData
    ) {
        User user = userService.getById(userData.getUserId());

        // Build UserInfo
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.getRole().name(),
                user.getCreatedOn(),
                user.getUpdatedOn()
        );

        // Get all cars for user
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

        // Get all maintenances for user
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

        // Calculate statistics
        int totalCars = carInfos.size();
        int totalMaintenances = maintenanceInfos.size();
        BigDecimal totalMaintenanceCost = maintenances.stream()
                .filter(m -> m.getCost() != null)
                .map(Maintenance::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build UserProfileData
        UserProfileData userProfileData = new UserProfileData(
                userInfo,
                carInfos,
                maintenanceInfos,
                totalCars,
                totalMaintenances,
                totalMaintenanceCost,
                LocalDateTime.now()
        );

        // Generate PDF with full profile data
        byte[] pdf = avatarPdfService.generatePdfWithProfile(
                file,
                name != null ? name : user.getUsername(),
                userProfileData
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=user-profile.pdf")
                .body(pdf);
    }

    @GetMapping("/latest")
    public ModelAndView viewLatestPdfInfo(@AuthenticationPrincipal UserData userData) {
        try {
            AvatarPdfResponse pdfInfo = avatarPdfService.getLatestPdfForUser(userData.getUserId());

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("pdf-info");
            modelAndView.addObject("pdfInfo", pdfInfo);
            modelAndView.addObject("user", userService.getById(userData.getUserId()));

            return modelAndView;
        } catch (feign.FeignException.NotFound e) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("pdf-info");
            modelAndView.addObject("noPdf", true);
            modelAndView.addObject("user", userService.getById(userData.getUserId()));
            return modelAndView;
        } catch (Exception e) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("pdf-info");
            modelAndView.addObject("error", "An error occurred while fetching PDF information.");
            modelAndView.addObject("user", userService.getById(userData.getUserId()));
            return modelAndView;
        }
    }

    @PostMapping("/delete-latest")
    public String deleteLatestPdf(@AuthenticationPrincipal UserData userData) {
        try {
            avatarPdfService.deleteLatestPdfForUser(userData.getUserId());
            return "redirect:/pdf/generate?deleteSuccess=true";
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No PDF found")) {
                return "redirect:/pdf/generate?deleteError=No PDF found";
            }
            return "redirect:/pdf/generate?deleteError=true";
        } catch (Exception e) {
            return "redirect:/pdf/generate?deleteError=true";
        }
    }
}

