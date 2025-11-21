package app.web;

import app.exception.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, RedirectAttributes redirectAttributes){

        redirectAttributes.addFlashAttribute("errorList", e.getErrors());
        return "redirect:/register";
    }

    @ExceptionHandler(MaintenanceCreateException.class)
    public String handleReqFieldsToCreateException(MaintenanceCreateException e, RedirectAttributes redirectAttributes){

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/new-maintenance";
    }

    @ExceptionHandler(MaintenanceUpdateException.class)
    public String handleReqFieldsToUpdateException(
            MaintenanceUpdateException e,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/maintenance/" + e.getMaintenanceId() + "/edit";
    }

    @ExceptionHandler(CarUpdateException.class)
    public String handleCarUpdateException(CarUpdateException e,
                                           RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/cars/" + e.getCarId() + "/edit";
    }

    @ExceptionHandler(CarCreateException.class)
    public ModelAndView handleReqFieldsToCreateCarException(CarCreateException e) {

        ModelAndView modelAndView = new ModelAndView("new-car");
        modelAndView.addObject("car", e.getCar());
        modelAndView.addObject("errorMessage", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(ProfileUpdateException.class)
    public ModelAndView handleProfileUpdateException(ProfileUpdateException e) {

        ModelAndView modelAndView = new ModelAndView("profile");

        modelAndView.addObject("user", e.getUser());
        modelAndView.addObject("editProfileRequest", e.getEditProfileRequest());
        modelAndView.addObject("errorMessage", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleLeftOverExceptions(Exception e){

        return new ModelAndView("internal-server-error");
    }

}
