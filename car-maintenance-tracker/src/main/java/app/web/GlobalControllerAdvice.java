package app.web;

import app.exception.DomainException;
import app.exception.ValidationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalControllerAdvice {


    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, RedirectAttributes redirectAttributes){

        redirectAttributes.addFlashAttribute("errorList", e.getErrors());
        return "redirect:/register";
    }

    @ExceptionHandler(DomainException.class)
    public String handleDomainException(DomainException e, RedirectAttributes redirectAttributes){

        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/new-car";
    }

}
