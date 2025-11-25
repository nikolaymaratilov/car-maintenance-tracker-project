package app.web;

import app.avatarPdf.AvatarPdfService;
import app.avatarPdf.dto.AvatarPdfResponse;
import app.security.UserData;
import app.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/pdf")
public class PdfController {

    private final AvatarPdfService avatarPdfService;
    private final UserService userService;

    public PdfController(AvatarPdfService avatarPdfService, UserService userService) {
        this.avatarPdfService = avatarPdfService;
        this.userService = userService;
    }

    @GetMapping("/generate")
    public String generatePdfPage() {
        return "pdf-generate";
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateFullProfilePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @AuthenticationPrincipal UserData userData) {
        
        String displayName = name != null ? name : userService.getById(userData.getUserId()).getUsername();
        byte[] pdf = avatarPdfService.generatePdfWithProfile(
                file,
                displayName,
                avatarPdfService.buildUserProfileData(userData.getUserId()));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=user-profile.pdf")
                .body(pdf);
    }

    @GetMapping("/latest")
    public ModelAndView viewLatestPdfInfo(@AuthenticationPrincipal UserData userData) {
        AvatarPdfResponse pdfInfo = avatarPdfService.getLatestPdfForUser(userData.getUserId());
        ModelAndView modelAndView = new ModelAndView("pdf-info");
        modelAndView.addObject("pdfInfo", pdfInfo);
        modelAndView.addObject("user", userService.getById(userData.getUserId()));
        return modelAndView;
    }

    @PostMapping("/delete-latest")
    public String deleteLatestPdf(@AuthenticationPrincipal UserData userData) {
        avatarPdfService.deleteLatestPdfForUser(userData.getUserId());
        return "redirect:/pdf/generate?deleteSuccess=true";
    }
}

