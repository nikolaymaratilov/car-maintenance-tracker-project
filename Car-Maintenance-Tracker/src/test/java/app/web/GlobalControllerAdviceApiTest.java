package app.web;

import app.car.model.Car;
import app.exception.*;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@Import(GlobalControllerAdvice.class)
class GlobalControllerAdviceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @GetMapping("/test/validation")
    public String throwValidationException() {
        throw new ValidationException(List.of("Error 1", "Error 2"));
    }

    @GetMapping("/test/maintenance-create")
    public String throwMaintenanceCreateException() {
        throw MaintenanceCreateException.blankEntitiesForMaintenance();
    }

    @GetMapping("/test/maintenance-update")
    public String throwMaintenanceUpdateException() {
        throw MaintenanceUpdateException.blankEntitiesForMaintenance(UUID.randomUUID());
    }

    @GetMapping("/test/car-update")
    public String throwCarUpdateException() {
        throw CarUpdateException.requiredFieldsForCar(UUID.randomUUID());
    }

    @GetMapping("/test/car-create")
    public String throwCarCreateException() {
        Car car = new Car();
        throw CarCreateException.requiredFieldsForCar(car);
    }

    @GetMapping("/test/profile-update")
    public String throwProfileUpdateException() {
        User user = new User();
        EditProfileRequest request = EditProfileRequest.builder().build();
        throw new ProfileUpdateException("Profile update failed", user, request);
    }

    @GetMapping("/test/feign-not-found")
    public String throwFeignNotFoundException() {
        throw new FeignException.NotFound("Not found", null, null, null);
    }

    @GetMapping("/test/generic-exception")
    public String throwGenericException() {
        throw new RuntimeException("Generic error");
    }

    private UsernamePasswordAuthenticationToken auth(UUID id) {
        UserData principal = new UserData(id, UserRole.USER, "testuser", "pass", true);
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    @Test
    void handleValidationException_shouldRedirectToRegister() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/validation")
                        .with(authentication(auth(id))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("errorList"));
    }

    @Test
    void handleMaintenanceCreateException_shouldRedirectToNewMaintenance() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/maintenance-create")
                        .with(authentication(auth(id))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/new-maintenance"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void handleMaintenanceUpdateException_shouldRedirectToEditMaintenance() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/maintenance-update")
                        .with(authentication(auth(id))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/maintenance/*/edit"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void handleCarUpdateException_shouldRedirectToEditCar() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/car-update")
                        .with(authentication(auth(id))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cars/*/edit"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void handleCarCreateException_shouldReturnNewCarView() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/car-create")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("new-car"))
                .andExpect(model().attributeExists("car"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void handleProfileUpdateException_shouldReturnProfileView() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/profile-update")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void handleGenericException_shouldReturnInternalServerErrorView() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/test/generic-exception")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("internal-server-error"));
    }
}

@Controller
@RequestMapping("/test")
class TestController {
    @GetMapping("/validation")
    public String throwValidationException() {
        throw new ValidationException(List.of("Error 1", "Error 2"));
    }

    @GetMapping("/maintenance-create")
    public String throwMaintenanceCreateException() {
        throw MaintenanceCreateException.blankEntitiesForMaintenance();
    }

    @GetMapping("/maintenance-update")
    public String throwMaintenanceUpdateException() {
        throw MaintenanceUpdateException.blankEntitiesForMaintenance(UUID.randomUUID());
    }

    @GetMapping("/car-update")
    public String throwCarUpdateException() {
        throw CarUpdateException.requiredFieldsForCar(UUID.randomUUID());
    }

    @GetMapping("/car-create")
    public String throwCarCreateException() {
        Car car = new Car();
        throw CarCreateException.requiredFieldsForCar(car);
    }

    @GetMapping("/profile-update")
    public String throwProfileUpdateException() {
        User user = new User();
        EditProfileRequest request = EditProfileRequest.builder().build();
        throw new ProfileUpdateException("Profile update failed", user, request);
    }

    @GetMapping("/feign-not-found")
    public String throwFeignNotFoundException() {
        throw new FeignException.NotFound("Not found", null, null, null);
    }

    @GetMapping("/generic-exception")
    public String throwGenericException() {
        throw new RuntimeException("Generic error");
    }
}

