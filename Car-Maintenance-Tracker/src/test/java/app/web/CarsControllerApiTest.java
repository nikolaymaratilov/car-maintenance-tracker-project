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

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CarsController.class)
class CarsControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private MaintenanceService maintenanceService;

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
    void getCars_shouldReturnCarsViewAndModel() throws Exception {

        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setBrand("BMW");
        car.setModel("M3");

        when(userService.getById(id)).thenReturn(user);
        when(carService.getCarsForUser(user)).thenReturn(List.of(car));
        when(carService.getLatestAdditions(any())).thenReturn(1);
        when(carService.getBrandModelsMap(any())).thenReturn(Map.of("BMW", Set.of("M3")));
        when(carService.filterCars(eq(user), any(), any(), any())).thenReturn(List.of(car));
        when(carService.getMaintenanceCountsMap(any(), any())).thenReturn(Map.of(car.getId(), 2));
        when(maintenanceService.upcomingNext30Days(user)).thenReturn(List.of());
        when(maintenanceService.getServiceReadyCount(user)).thenReturn(0L);

        mockMvc.perform(get("/cars").with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("cars"))
                .andExpect(model().attributeExists("cars"));
    }

    @Test
    void getNewCar_shouldReturnNewCarView() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/new-car")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("new-car"))
                .andExpect(model().attributeExists("car"));
    }


    @Test
    void addCar_valid_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/new-car")
                        .param("brand", "BMW")
                        .param("model", "M3")
                        .param("year", "2020")
                        .param("vin", "12345678901234567")
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));

        verify(carService).createCar(any(Car.class), eq(user));
    }

    @Test
    void deleteCar_shouldRedirectAndInvokeService() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(uid);

        when(userService.getById(uid)).thenReturn(user);

        mockMvc.perform(delete("/cars/" + carId)
                        .with(authentication(auth(uid)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));

        verify(carService).deleteCar(carId, user);
    }

    @Test
    void getCarDetails_shouldReturnView() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(uid);

        Car car = new Car();
        car.setId(carId);
        car.setBrand("Audi");
        car.setModel("A4");

        Maintenance m = new Maintenance();
        m.setDate(LocalDate.now());
        m.setType(MaintenanceType.OIL_CHANGE);

        when(userService.getById(uid)).thenReturn(user);
        when(carService.getCarForUser(carId, user)).thenReturn(car);
        when(maintenanceService.listForCar(carId)).thenReturn(List.of(m));

        mockMvc.perform(get("/cars/" + carId).with(authentication(auth(uid))))
                .andExpect(status().isOk())
                .andExpect(view().name("car-details"))
                .andExpect(model().attributeExists("car"))
                .andExpect(model().attributeExists("maintenances"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    void editCarForm_shouldReturnEditCarView() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(uid);

        Car car = new Car();
        car.setId(carId);

        when(userService.getById(uid)).thenReturn(user);
        when(carService.getCarForUser(carId, user)).thenReturn(car);

        mockMvc.perform(get("/cars/" + carId + "/edit").with(authentication(auth(uid))))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-car"))
                .andExpect(model().attributeExists("car"));
    }

    @Test
    void updateCar_valid_shouldRedirect() throws Exception {
        UUID uid = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        User user = fakeUser(uid);

        when(userService.getById(uid)).thenReturn(user);

        mockMvc.perform(put("/cars/" + carId)
                        .param("brand", "Audi")
                        .param("model", "A4")
                        .param("year", "2018")
                        .param("vin", "ABCDEFG1234567890")
                        .with(authentication(auth(uid)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars/" + carId));

        verify(carService).updateCar(eq(carId), eq(user), any(Car.class));
    }
}
