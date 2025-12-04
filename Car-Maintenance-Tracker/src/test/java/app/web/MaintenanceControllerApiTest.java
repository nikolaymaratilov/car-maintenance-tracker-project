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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceController.class)
class MaintenanceControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MaintenanceService maintenanceService;

    @MockitoBean
    private CarService carService;

    private User fakeUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        u.setPassword("pass");
        u.setEmail("john@mail.com");
        u.setEnabled(true);
        u.setRole(UserRole.USER);
        u.setCreatedOn(java.time.LocalDateTime.now());
        u.setUpdatedOn(java.time.LocalDateTime.now());
        return u;
    }

    private UsernamePasswordAuthenticationToken auth(UUID id) {
        UserData principal = new UserData(id, UserRole.USER, "john", "pass", true);
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    @Test
    void getMaintenancePage_shouldReturnMaintenanceViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setBrand("BMW");
        car.setModel("M3");

        Maintenance maintenance = new Maintenance();
        maintenance.setId(UUID.randomUUID());
        maintenance.setDate(LocalDate.now());
        maintenance.setType(MaintenanceType.OIL_CHANGE);
        maintenance.setCost(BigDecimal.valueOf(50.00));
        maintenance.setCar(car);

        when(userService.getById(id)).thenReturn(user);
        when(maintenanceService.filterForUser(eq(user), any(), any(), any())).thenReturn(List.of(maintenance));
        when(maintenanceService.upcomingNext30Days(user)).thenReturn(List.of());
        when(maintenanceService.getThisMonthCount(anyList())).thenReturn(1L);
        when(maintenanceService.getTotalSpent(anyList())).thenReturn(BigDecimal.valueOf(50.00));
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));

        mockMvc.perform(get("/maintenance").with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("maintenance"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("maintenances"))
                .andExpect(model().attributeExists("totalRecords"))
                .andExpect(model().attributeExists("thisMonthCount"))
                .andExpect(model().attributeExists("totalSpent"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("types"))
                .andExpect(model().attributeExists("upcomingMaintenances"));
    }

    @Test
    void getMaintenancePage_withFilters_shouldReturnFilteredResults() throws Exception {
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(id);

        Car car = new Car();
        car.setId(carId);
        car.setBrand("BMW");
        car.setModel("M3");

        Maintenance maintenance = new Maintenance();
        maintenance.setId(UUID.randomUUID());
        maintenance.setType(MaintenanceType.OIL_CHANGE);
        maintenance.setCar(car);

        when(userService.getById(id)).thenReturn(user);
        when(maintenanceService.filterForUser(eq(user), eq(carId), eq(MaintenanceType.OIL_CHANGE), eq("oil")))
                .thenReturn(List.of(maintenance));
        when(maintenanceService.upcomingNext30Days(user)).thenReturn(List.of());
        when(maintenanceService.getThisMonthCount(anyList())).thenReturn(1L);
        when(maintenanceService.getTotalSpent(anyList())).thenReturn(BigDecimal.ZERO);
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));

        mockMvc.perform(get("/maintenance")
                        .param("carId", carId.toString())
                        .param("type", "OIL_CHANGE")
                        .param("search", "oil")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("maintenance"))
                .andExpect(model().attribute("selectedCarId", carId))
                .andExpect(model().attribute("selectedType", MaintenanceType.OIL_CHANGE))
                .andExpect(model().attribute("search", "oil"));
    }

    @Test
    void getNewMaintenance_shouldReturnNewMaintenanceView() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        Maintenance maintenance = new Maintenance();
        Car car = new Car();
        car.setId(UUID.randomUUID());

        when(userService.getById(id)).thenReturn(user);
        when(maintenanceService.prepareNewMaintenanceForm(user, null)).thenReturn(maintenance);
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));

        mockMvc.perform(get("/new-maintenance").with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("new-maintenance"))
                .andExpect(model().attributeExists("maintenance"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    void getNewMaintenance_withCarId_shouldReturnNewMaintenanceView() throws Exception {
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(id);

        Maintenance maintenance = new Maintenance();
        Car car = new Car();
        car.setId(carId);

        when(userService.getById(id)).thenReturn(user);
        when(maintenanceService.prepareNewMaintenanceForm(user, carId)).thenReturn(maintenance);
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));

        mockMvc.perform(get("/new-maintenance")
                        .param("carId", carId.toString())
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("new-maintenance"))
                .andExpect(model().attributeExists("maintenance"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    void addMaintenance_valid_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/new-maintenance")
                        .param("date", "2024-01-15")
                        .param("type", "OIL_CHANGE")
                        .param("mileage", "50000")
                        .param("cost", "50.00")
                        .param("car.id", carId.toString())
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maintenance"));

        verify(maintenanceService).createMaintenance(any(Maintenance.class));
    }

//    @Test
//    void addMaintenance_invalid_shouldReturnNewMaintenanceView() throws Exception {
//        UUID id = UUID.randomUUID();
//        User user = fakeUser(id);
//
//        Car car = new Car();
//        car.setId(UUID.randomUUID());
//
//        when(userService.getById(id)).thenReturn(user);
//        when(carService.getCarsForUser(user)).thenReturn(List.of(car));
//
//        mockMvc.perform(post("/new-maintenance")
//                        .param("date", "")
//                        .param("mileage", "-1")
//                        .with(authentication(auth(id)))
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(view().name("new-maintenance"))
//                .andExpect(model().attributeExists("maintenance"))
//                .andExpect(model().attributeExists("cars"))
//                .andExpect(model().attributeExists("types"));
//
//        verify(maintenanceService, never()).createMaintenance(any());
//    }

    @Test
    void deleteMaintenance_shouldRedirectAndInvokeService() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID maintenanceId = UUID.randomUUID();
        User user = fakeUser(uid);

        when(userService.getById(uid)).thenReturn(user);

        mockMvc.perform(delete("/maintenance/" + maintenanceId)
                        .with(authentication(auth(uid)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maintenance"));

        verify(maintenanceService).deleteMaintenanceForUser(maintenanceId, user);
    }

    @Test
    void editMaintenanceForm_shouldReturnEditMaintenanceView() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID maintenanceId = UUID.randomUUID();
        User user = fakeUser(uid);

        Maintenance maintenance = new Maintenance();
        maintenance.setId(maintenanceId);
        maintenance.setType(MaintenanceType.OIL_CHANGE);
        maintenance.setDate(LocalDate.now());

        Car car = new Car();
        car.setId(UUID.randomUUID());

        when(userService.getById(uid)).thenReturn(user);
        when(maintenanceService.getForUser(maintenanceId, user)).thenReturn(maintenance);
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));

        mockMvc.perform(get("/maintenance/" + maintenanceId + "/edit")
                        .with(authentication(auth(uid))))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-maintenance"))
                .andExpect(model().attributeExists("maintenance"))
                .andExpect(model().attributeExists("cars"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    void updateMaintenance_valid_shouldRedirect() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID maintenanceId = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(uid);

        when(userService.getById(uid)).thenReturn(user);

        mockMvc.perform(put("/maintenance/" + maintenanceId)
                        .param("date", "2024-01-20")
                        .param("type", "TIRE_ROTATION")
                        .param("mileage", "55000")
                        .param("cost", "75.00")
                        .param("car.id", carId.toString())
                        .with(authentication(auth(uid)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/maintenance"));

        verify(maintenanceService).update(eq(maintenanceId), eq(user), any(Maintenance.class));
    }

//    @Test
//    void updateMaintenance_invalid_shouldReturnEditMaintenanceView() throws Exception {
//        UUID uid = UUID.randomUUID();
//        UUID maintenanceId = UUID.randomUUID();
//        User user = fakeUser(uid);
//
//        Car car = new Car();
//        car.setId(UUID.randomUUID());
//
//        when(userService.getById(uid)).thenReturn(user);
//        when(carService.getCarsForUser(user)).thenReturn(List.of(car));
//
//        mockMvc.perform(put("/maintenance/" + maintenanceId)
//                        .param("date", "")
//                        .param("mileage", "-1")
//                        .with(authentication(auth(uid)))
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(view().name("edit-maintenance"))
//                .andExpect(model().attributeExists("maintenance"))
//                .andExpect(model().attributeExists("cars"))
//                .andExpect(model().attributeExists("types"));
//
//        verify(maintenanceService, never()).update(any(), any(), any());
//    }
}

