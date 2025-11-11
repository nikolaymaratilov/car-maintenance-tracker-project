package app.web;

import app.exception.DomainException;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal UserData userData){
        User user = userService.getById(userData.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);

        EditProfileRequest editProfileRequest = new EditProfileRequest();
        editProfileRequest.setUsername(user.getUsername());
        editProfileRequest.setEmail(user.getEmail());
        editProfileRequest.setProfilePictureUrl(user.getProfilePictureUrl());

        modelAndView.addObject("editProfileRequest", editProfileRequest);

        return modelAndView;
    }

    @PostMapping("/profile/edit")
    public ModelAndView editProfile(
            @Valid @ModelAttribute("editProfileRequest") EditProfileRequest editProfileRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            return modelAndView;
        }

        try {
            userService.updateProfile(user, editProfileRequest);
        } catch (DomainException e) {

            ModelAndView mv = new ModelAndView("profile");
            mv.addObject("user", user);
            mv.addObject("editProfileRequest", editProfileRequest);
            mv.addObject("errorMessage", e.getMessage());
            return mv;
        }

        return new ModelAndView("redirect:/home");
    }
}
