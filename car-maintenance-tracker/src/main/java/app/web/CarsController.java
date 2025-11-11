package app.web;

import app.car.model.Car;
import app.car.service.CarService;
import app.exception.DomainException;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarsController {

    private final UserService userService;
    private final CarService carService;

    public CarsController(UserService userService, CarService carService) {
        this.userService = userService;
        this.carService = carService;
    }

    @GetMapping
    private ModelAndView getCars(@AuthenticationPrincipal UserData userData){

        User user = userService.getById(userData.getUserId());

        List<Car> cars = carService.getCarsForUser(user);
        int countOfLatestAdditions = carService.getLatestAdditions(cars);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cars");
        modelAndView.addObject("user",user);
        modelAndView.addObject("cars",cars);
        modelAndView.addObject("countOfLatestAdditions",countOfLatestAdditions);

        return modelAndView;
    }

    @GetMapping("/new-car")
    private ModelAndView getNewCar(){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-car");
        modelAndView.addObject("car",new Car());

        return modelAndView;
    }

    @PostMapping("/new-car")
    private ModelAndView addCar(@AuthenticationPrincipal UserData userData, @ModelAttribute("car") Car car, BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-car");
            modelAndView.addObject("car", car);
            return modelAndView;
        }

        User user = userService.getById(userData.getUserId());

        try {
            carService.createCar(car,user);
            return new ModelAndView("redirect:/cars");

        } catch (DomainException e){
            ModelAndView modelAndView = new ModelAndView("new-car");
            modelAndView.addObject("car",car);

            modelAndView.addObject("errorMessage",e.getMessage());

            return  modelAndView;
        }
    }
}
