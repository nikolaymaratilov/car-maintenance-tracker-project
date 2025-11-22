package app.web;

import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class AdminController {

    private final UserService userService;
    private final CarService carService;
    private final MaintenanceService maintenanceService;

    public AdminController(UserService userService, CarService carService, MaintenanceService maintenanceService) {
        this.userService = userService;
        this.carService = carService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {

        ArrayList<Integer> usersAndAdminsCount = userService.getAllUsers();
        List<Car> cars = carService.getAll();
        List<Maintenance> maintenances = maintenanceService.getAll();

        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("usersAndAdminsCount",usersAndAdminsCount);
        modelAndView.addObject("cars",cars);
        modelAndView.addObject("maintenance",maintenances);
        return modelAndView;
    }

    @GetMapping("/admin-users")
    public ModelAndView getAllUsers() {
        List<User> users = userService.getAllUsersList();
        ModelAndView modelAndView = new ModelAndView("admin-users");
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @GetMapping("/admin-cars")
    public ModelAndView getAllCars() {
        List<Car> cars = carService.getAll();
        ModelAndView modelAndView = new ModelAndView("admin-cars");
        modelAndView.addObject("cars", cars);
        return modelAndView;
    }

    @GetMapping("/admin-maintenances")
    public ModelAndView getAllMaintenances() {
        List<Maintenance> maintenances = maintenanceService.getAll();
        ModelAndView modelAndView = new ModelAndView("admin-maintenances");
        modelAndView.addObject("maintenances", maintenances);
        return modelAndView;
    }

    @PostMapping("/admin/users/{userId}/switch-role")
    public String switchUserRole(@PathVariable UUID userId) {

        userService.switchRole(userId);
        return "redirect:/admin-users";
    }

    @PostMapping("/admin/users/{userId}/switch-status")
    public String switchUserStatus(@PathVariable UUID userId) {

        userService.switchStatus(userId);
        return "redirect:/admin-users";
    }
}

