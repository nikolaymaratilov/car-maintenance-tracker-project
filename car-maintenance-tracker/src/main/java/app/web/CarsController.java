package app.web;

import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.model.MaintenanceType;
import app.maintenance.service.MaintenanceService;
import app.exception.DomainException;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
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

import java.util.*;

@Controller
public class CarsController {

    private final UserService userService;
    private final CarService carService;
    private final MaintenanceService maintenanceService;

    public CarsController(UserService userService, CarService carService, MaintenanceService maintenanceService) {
        this.userService = userService;
        this.carService = carService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/cars")
    public ModelAndView getCars(@AuthenticationPrincipal UserData userData,
                                @RequestParam(required = false) String brand,
                                @RequestParam(required = false) String model,
                                @RequestParam(required = false) String search){

        User user = userService.getById(userData.getUserId());
        List<Car> cars = carService.getCarsForUser(user);
        int countOfLatestAdditions = carService.getLatestAdditions(cars);
        Map<String, Set<String>> brandModels = carService.getBrandModelsMap(cars);
        List<Car> filteredCars = carService.filterCars(user, brand, model, search);
        Map<UUID, Integer> maintenanceCounts = carService.getMaintenanceCountsMap(cars, maintenanceService);
        List<app.maintenance.model.Maintenance> upcomingNext30Days = maintenanceService.upcomingNext30Days(user);
        long serviceReadyCount = maintenanceService.getServiceReadyCount(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cars");
        modelAndView.addObject("user",user);
        modelAndView.addObject("cars",cars);
        modelAndView.addObject("brandModels",brandModels);
        modelAndView.addObject("filteredCars",filteredCars);
        modelAndView.addObject("countOfLatestAdditions",countOfLatestAdditions);
        modelAndView.addObject("brand",brand);
        modelAndView.addObject("model",model);
        modelAndView.addObject("search",search);
        modelAndView.addObject("maintenanceCounts", maintenanceCounts);
        modelAndView.addObject("serviceReadyCount", serviceReadyCount);
        modelAndView.addObject("upcomingMaintenances", upcomingNext30Days);

        return modelAndView;
    }

    @GetMapping("/new-car")
    public ModelAndView getNewCar(){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-car");
        modelAndView.addObject("car",new Car());

        return modelAndView;
    }

    @PostMapping("/new-car")
    public ModelAndView addCar(@AuthenticationPrincipal UserData userData, @ModelAttribute("car") Car car, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-car");
            modelAndView.addObject("car", car);
            return modelAndView;
        }

        User user = userService.getById(userData.getUserId());

            carService.createCar(car,user);
            return new ModelAndView("redirect:/cars");

    }

    @DeleteMapping("/cars/{carId}")
    public ModelAndView deleteCar(@AuthenticationPrincipal UserData userData, @PathVariable UUID carId) {
        User user = userService.getById(userData.getUserId());
        carService.deleteCar(carId, user);
        return new ModelAndView("redirect:/cars");
    }

    @GetMapping("/cars/{carId}")
    public ModelAndView getCarDetails(@AuthenticationPrincipal UserData userData,
                                      @PathVariable UUID carId) {
        User user = userService.getById(userData.getUserId());
        Car car = carService.getCarForUser(carId, user);
        List<Maintenance> maintenances = maintenanceService.listForCar(carId);

        ModelAndView modelAndView = new ModelAndView("car-details");
        modelAndView.addObject("user", user);
        modelAndView.addObject("car", car);
        modelAndView.addObject("maintenances", maintenances);
        modelAndView.addObject("types", MaintenanceType.values());

        return modelAndView;
    }

    @GetMapping("/cars/{carId}/edit")
    public ModelAndView editCarForm(@AuthenticationPrincipal UserData userData,
                                    @PathVariable UUID carId) {
        User user = userService.getById(userData.getUserId());
        Car car = carService.getCarForUser(carId, user);

        ModelAndView modelAndView = new ModelAndView("edit-car");
        modelAndView.addObject("car", car);

        return modelAndView;
    }

    @PutMapping("/cars/{carId}")
    public ModelAndView updateCar(@AuthenticationPrincipal UserData userData,
                                  @PathVariable UUID carId,
                                  @ModelAttribute("car") Car car,
                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("edit-car");
            modelAndView.addObject("car", car);
            return modelAndView;
        }

        User user = userService.getById(userData.getUserId());

        try {
            carService.updateCar(carId, user, car);
            return new ModelAndView("redirect:/cars/" + carId);
        } catch (DomainException e) {
            ModelAndView modelAndView = new ModelAndView("edit-car");
            modelAndView.addObject("car", car);
            modelAndView.addObject("errorMessage", e.getMessage());
            return modelAndView;
        }
        //todo
    }
}
