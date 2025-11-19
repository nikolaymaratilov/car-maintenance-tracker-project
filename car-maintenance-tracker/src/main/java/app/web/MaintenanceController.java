package app.web;

import app.car.service.CarService;
import app.exception.DomainException;
import app.maintenance.model.Maintenance;
import app.maintenance.model.MaintenanceType;
import app.maintenance.service.MaintenanceService;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MaintenanceController {

    private final UserService userService;
    private final MaintenanceService maintenanceService;
    private final CarService carService;

    public MaintenanceController(UserService userService,
                                 MaintenanceService maintenanceService,
                                 CarService carService) {
        this.userService = userService;
        this.maintenanceService = maintenanceService;
        this.carService = carService;
    }

    @GetMapping("/maintenance")
    public ModelAndView getMaintenancePage(@AuthenticationPrincipal UserData userData,
                                           @RequestParam(required = false) UUID carId,
                                           @RequestParam(required = false) MaintenanceType type,
                                           @RequestParam(required = false) String search){

        User user = userService.getById(userData.getUserId());
        List<Maintenance> maintenances = maintenanceService.filterForUser(user, carId, type, search);
        List<Maintenance> upcomingNext30Days = maintenanceService.upcomingNext30Days(user);
        int totalRecords = maintenances.size();
        long thisMonthCount = maintenanceService.getThisMonthCount(maintenances);
        BigDecimal totalSpent = maintenanceService.getTotalSpent(maintenances);

        ModelAndView modelAndView = new ModelAndView("maintenance");
        modelAndView.addObject("user",user);
        modelAndView.addObject("maintenances", maintenances);
        modelAndView.addObject("totalRecords", totalRecords);
        modelAndView.addObject("thisMonthCount", thisMonthCount);
        modelAndView.addObject("totalSpent", totalSpent);
        modelAndView.addObject("cars", carService.getCarsForUser(user));
        modelAndView.addObject("types", MaintenanceType.values());
        modelAndView.addObject("selectedCarId", carId);
        modelAndView.addObject("selectedType", type);
        modelAndView.addObject("search", search);
        modelAndView.addObject("upcomingMaintenances", upcomingNext30Days);

        return modelAndView;
    }

    @GetMapping("/new-maintenance")
    public ModelAndView getNewMaintenance(@AuthenticationPrincipal UserData userData,
                                          @RequestParam(required = false) UUID carId){

        User user = userService.getById(userData.getUserId());

        Maintenance maintenance = maintenanceService.prepareNewMaintenanceForm(user, carId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-maintenance");
        modelAndView.addObject("maintenance", maintenance);
        modelAndView.addObject("cars", carService.getCarsForUser(user));
        modelAndView.addObject("types", MaintenanceType.values());

        return modelAndView;
    }

    @PostMapping("/new-maintenance")
    public ModelAndView addMaintenance(@AuthenticationPrincipal UserData userData,
                                       @ModelAttribute("maintenance") Maintenance maintenance,
                                       BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            User user = userService.getById(userData.getUserId());

            ModelAndView modelAndView = new ModelAndView("new-maintenance");
            modelAndView.addObject("maintenance", maintenance);
            modelAndView.addObject("cars", carService.getCarsForUser(user));
            modelAndView.addObject("types", MaintenanceType.values());
            return modelAndView;
        }

        //todo
        try {
            maintenanceService.createMaintenance(maintenance);
            return new ModelAndView("redirect:/maintenance");

        } catch (DomainException e){
            User user = userService.getById(userData.getUserId());

            ModelAndView modelAndView = new ModelAndView("new-maintenance");
            modelAndView.addObject("maintenance",maintenance);
            modelAndView.addObject("cars", carService.getCarsForUser(user));
            modelAndView.addObject("types", MaintenanceType.values());
            modelAndView.addObject("errorMessage",e.getMessage());

            return  modelAndView;
        }
    }

    @DeleteMapping("/maintenance/{maintenanceId}")
    public ModelAndView deleteMaintenance(@AuthenticationPrincipal UserData userData,
                                          @PathVariable UUID maintenanceId) {
        User user = userService.getById(userData.getUserId());

        maintenanceService.deleteMaintenanceForUser(maintenanceId, user);

        return new ModelAndView("redirect:/maintenance");
    }

    @GetMapping("/maintenance/{maintenanceId}/edit")
    public ModelAndView editMaintenanceForm(@AuthenticationPrincipal UserData userData,
                                            @PathVariable UUID maintenanceId) {
        User user = userService.getById(userData.getUserId());
        Maintenance maintenance = maintenanceService.getForUser(maintenanceId, user);

        ModelAndView modelAndView = new ModelAndView("edit-maintenance");
        modelAndView.addObject("maintenance", maintenance);
        modelAndView.addObject("cars", carService.getCarsForUser(user));
        modelAndView.addObject("types", MaintenanceType.values());

        return modelAndView;
    }

    @PutMapping("/maintenance/{maintenanceId}")
    public ModelAndView updateMaintenance(@AuthenticationPrincipal UserData userData,
                                          @PathVariable UUID maintenanceId,
                                          @ModelAttribute("maintenance") Maintenance maintenance,
                                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(userData.getUserId());

            ModelAndView modelAndView = new ModelAndView("edit-maintenance");
            modelAndView.addObject("maintenance", maintenance);
            modelAndView.addObject("cars", carService.getCarsForUser(user));
            modelAndView.addObject("types", MaintenanceType.values());
            return modelAndView;
        }

        User user = userService.getById(userData.getUserId());

        //todo
        try {
            maintenanceService.update(maintenanceId, user, maintenance);
            return new ModelAndView("redirect:/maintenance");
        } catch (DomainException e) {
            ModelAndView modelAndView = new ModelAndView("edit-maintenance");
            modelAndView.addObject("maintenance", maintenance);
            modelAndView.addObject("cars", carService.getCarsForUser(user));
            modelAndView.addObject("types", MaintenanceType.values());
            modelAndView.addObject("errorMessage", e.getMessage());

            return modelAndView;
        }
    }
}
