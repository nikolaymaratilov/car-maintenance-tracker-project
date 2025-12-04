package app.web;

import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.model.MaintenanceType;
import app.maintenance.service.MaintenanceService;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private MaintenanceService maintenanceService;

    private UsernamePasswordAuthenticationToken authAdmin(UUID id) {
        UserData principal = new UserData(id, UserRole.ADMIN, "admin", "pass", true);
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    @Test
    void getDashboard_shouldReturnDashboardViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        ArrayList<Integer> usersAndAdminsCount = new ArrayList<>();
        usersAndAdminsCount.add(5);
        usersAndAdminsCount.add(2);

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setBrand("BMW");
        car.setModel("M3");

        Maintenance maintenance = new Maintenance();
        maintenance.setId(UUID.randomUUID());
        maintenance.setType(MaintenanceType.OIL_CHANGE);
        maintenance.setDate(LocalDate.now());

        when(userService.getAllUsers()).thenReturn(usersAndAdminsCount);
        when(carService.getAll()).thenReturn(List.of(car));
        when(maintenanceService.getAll()).thenReturn(List.of(maintenance));

        mockMvc.perform(get("/dashboard")
                        .with(authentication(authAdmin(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("usersAndAdminsCount"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("maintenance"));
    }

    @Test
    void getAllUsers_shouldReturnAdminUsersViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("user1");
        user1.setRole(UserRole.USER);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setRole(UserRole.ADMIN);

        when(userService.getAllUsersList()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin-users")
                        .with(authentication(authAdmin(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    void getAllCars_shouldReturnAdminCarsViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        Car car1 = new Car();
        car1.setId(UUID.randomUUID());
        car1.setBrand("BMW");
        car1.setModel("M3");

        Car car2 = new Car();
        car2.setId(UUID.randomUUID());
        car2.setBrand("Audi");
        car2.setModel("A4");

        when(carService.getAll()).thenReturn(List.of(car1, car2));

        mockMvc.perform(get("/admin-cars")
                        .with(authentication(authAdmin(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-cars"))
                .andExpect(model().attributeExists("cars"));
    }

    @Test
    void getAllMaintenances_shouldReturnAdminMaintenancesViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        Maintenance maintenance1 = new Maintenance();
        maintenance1.setId(UUID.randomUUID());
        maintenance1.setType(MaintenanceType.OIL_CHANGE);
        maintenance1.setDate(LocalDate.now());
        maintenance1.setCost(BigDecimal.valueOf(50.00));

        Maintenance maintenance2 = new Maintenance();
        maintenance2.setId(UUID.randomUUID());
        maintenance2.setType(MaintenanceType.TIRE_ROTATION);
        maintenance2.setDate(LocalDate.now().minusDays(5));
        maintenance2.setCost(BigDecimal.valueOf(75.00));

        when(maintenanceService.getAll()).thenReturn(List.of(maintenance1, maintenance2));

        mockMvc.perform(get("/admin-maintenances")
                        .with(authentication(authAdmin(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-maintenances"))
                .andExpect(model().attributeExists("maintenances"));
    }

    @Test
    void switchUserRole_shouldRedirectAndInvokeService() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/" + userId + "/switch-role")
                        .with(authentication(authAdmin(adminId)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin-users"));

        verify(userService).switchRole(userId);
    }

    @Test
    void switchUserStatus_shouldRedirectAndInvokeService() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/" + userId + "/switch-status")
                        .with(authentication(authAdmin(adminId)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin-users"));

        verify(userService).switchStatus(userId);
    }
}

