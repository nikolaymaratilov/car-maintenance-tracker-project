package app.web;

import app.exception.DomainException;
import app.exception.ValidationException;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getIndexPage(){

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest",new RegisterRequest());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult bindingResult){

        if (bindingResult.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("/register");
            modelAndView.addObject("registerRequest", registerRequest);
            return modelAndView;
        }

        try {
            userService.createNewUser(registerRequest);
            return new ModelAndView("redirect:/login");
        }
        catch (ValidationException e) {
            ModelAndView modelAndView = new ModelAndView("/register");
            modelAndView.addObject("registerRequest", registerRequest);

            modelAndView.addObject("errorList", e.getErrors());
            return modelAndView;
        }
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam (name= "loginAttemptMessage",required = false) String message,@RequestParam (name= "error",required = false) String errorMessage){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest",new LoginRequest());
        modelAndView.addObject("loginAttemptMessage",message);
        if (errorMessage != null){
            modelAndView.addObject("errorMessage","Invalid username or password");
        }

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal UserData userData){

        User user = userService.getById(userData.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user",user);
        return modelAndView;
    }
}