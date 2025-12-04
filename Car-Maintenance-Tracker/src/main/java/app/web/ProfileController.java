package app.web;

import app.avatarPdf.AvatarPdfService;
import app.avatarPdf.dto.AvatarPdfResponse;
import app.security.UserData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final AvatarPdfService avatarPdfService;

    @Autowired
    public ProfileController(UserService userService, AvatarPdfService avatarPdfService) {
        this.userService = userService;
        this.avatarPdfService = avatarPdfService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal UserData userData){
        User user = userService.getById(userData.getUserId());
        
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
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
            ModelAndView mv = new ModelAndView("profile");
            mv.addObject("user", user);
            mv.addObject("editProfileRequest", editProfileRequest);
            return mv;
        }

        userService.updateProfile(user, editProfileRequest);

        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/profile/avatar/pdf")
    public ResponseEntity<byte[]> generateAvatarPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) {
        byte[] pdf = avatarPdfService.generatePdf(file, name);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=avatar.pdf")
                .body(pdf);
    }


    @GetMapping("/profile/pdf/{id}")
    @ResponseBody
    public AvatarPdfResponse viewPdfInfo(@PathVariable UUID id) {
        return avatarPdfService.getPdf(id);
    }
}
