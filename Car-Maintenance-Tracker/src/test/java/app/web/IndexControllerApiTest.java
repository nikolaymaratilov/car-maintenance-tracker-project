package app.web;

import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.service.MaintenanceService;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
class IndexControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private MaintenanceService maintenanceService;

    @Test
    void getIndexPage_shouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRegisterPage_shouldReturnRegisterViewWithModel() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRegister_validInput_shouldRedirectToLogin() throws Exception {

        mockMvc.perform(post("/register")
                        .param("username", "John123")
                        .param("email", "john@mail.com")
                        .param("password", "secret123")
                        .param("repeatPassword", "secret123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).createNewUser(any(RegisterRequest.class));
    }


    @Test
    void postRegister_invalidInput_shouldReturnRegisterView() throws Exception {

        mockMvc.perform(post("/register")
                        .param("username", "")
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));

        verify(userService, never()).createNewUser(any());
    }

    @Test
    void getLoginPage_shouldReturnLoginView() throws Exception {

        when(userService.resolveLoginMessage(any(), any(), anyBoolean()))
                .thenReturn("Error!");

        mockMvc.perform(get("/login")
                        .param("loginAttemptMessage", "hello")
                        .param("error", "bad")
                        .param("disabled", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(userService).resolveLoginMessage("hello", "bad", true);
    }

    @Test
    void getHomePage_shouldReturnHomeViewWithModel() throws Exception {

        UUID id = UUID.randomUUID();
        UserData principal = new UserData(id, UserRole.USER, "john", "pass", true);

        User user = new User();
        user.setId(id);
        user.setRole(UserRole.USER);

        when(userService.getById(id)).thenReturn(user);
        when(carService.getCarsForUser(user)).thenReturn(List.of(new Car()));
        when(maintenanceService.upcomingNext30Days(user)).thenReturn(List.of(new Maintenance(), new Maintenance()));
        when(maintenanceService.getRecentMaintenances(user, 5)).thenReturn(List.of());
        when(maintenanceService.getMonthlyCost(user)).thenReturn(BigDecimal.TEN);

        mockMvc.perform(get("/home").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("upcomingMaintenance"))
                .andExpect(model().attributeExists("monthlyCost"))
                .andExpect(model().attributeExists("recentMaintenances"));
    }
}
