package app.avatarPdf;

import app.avatarPdf.dto.*;
import app.car.model.Car;
import app.car.service.CarService;
import app.maintenance.model.Maintenance;
import app.maintenance.model.MaintenanceType;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvatarPdfServiceTest {

    private AvatarPdfClient client;
    private ObjectMapper objectMapper;
    private UserService userService;
    private CarService carService;
    private MaintenanceService maintenanceService;
    private AvatarPdfService avatarPdfService;

    @BeforeEach
    void setup() {
        client = mock(AvatarPdfClient.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        userService = mock(UserService.class);
        carService = mock(CarService.class);
        maintenanceService = mock(MaintenanceService.class);
        avatarPdfService = new AvatarPdfService(client, objectMapper, userService, carService, maintenanceService);
    }

    @Test
    void generatePdf_shouldCallClient() {
        MultipartFile file = mock(MultipartFile.class);
        byte[] expected = "PDF".getBytes();

        when(client.createPdf(file, "Test")).thenReturn(expected);

        byte[] result = avatarPdfService.generatePdf(file, "Test");

        assertArrayEquals(expected, result);
        verify(client).createPdf(file, "Test");
    }

    @Test
    void generatePdfWithProfile_shouldSerializeAndCallClient() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        UserProfileData data = new UserProfileData(null, null, null, 0, 0, BigDecimal.ZERO, LocalDateTime.now());

        when(client.createPdfWithProfile(any(), any(), any())).thenReturn("OK".getBytes());

        byte[] result = avatarPdfService.generatePdfWithProfile(file, "Test", data);

        assertNotNull(result);
        verify(client).createPdfWithProfile(eq(file), eq("Test"), anyString());
    }

    @Test
    void generatePdfWithProfile_whenJsonProcessingFails_shouldThrowRuntimeException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        UserProfileData data = new UserProfileData(null, null, null, 0, 0, BigDecimal.ZERO, LocalDateTime.now());
        ObjectMapper failingMapper = mock(ObjectMapper.class);

        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        AvatarPdfService service = new AvatarPdfService(client, failingMapper, userService, carService, maintenanceService);

        assertThrows(RuntimeException.class, () -> {
            service.generatePdfWithProfile(file, "Test", data);
        });
    }

    @Test
    void getPdf_shouldReturnResponse() {
        UUID id = UUID.randomUUID();
        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setId(id);
        response.setDisplayName("test-pdf");

        when(client.getPdf(id)).thenReturn(response);

        AvatarPdfResponse result = avatarPdfService.getPdf(id);

        assertEquals(response, result);
        assertEquals(id, result.getId());
        assertEquals("test-pdf", result.getDisplayName());
        verify(client).getPdf(id);
    }

    @Test
    void getLatestPdfForUser_shouldReturnResponse() {
        UUID userId = UUID.randomUUID();
        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setUserId(userId);

        when(client.getLatestPdfForUser(userId)).thenReturn(response);

        AvatarPdfResponse result = avatarPdfService.getLatestPdfForUser(userId);

        assertEquals(response, result);
        assertEquals(userId, result.getUserId());
        verify(client).getLatestPdfForUser(userId);
    }

    @Test
    void deleteLatestPdfForUser_shouldCallClient() {
        UUID userId = UUID.randomUUID();

        avatarPdfService.deleteLatestPdfForUser(userId);

        verify(client).deleteLatestPdfForUserPost(userId);
    }

    @Test
    void buildUserProfileData_shouldBuildCorrectData() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setProfilePictureUrl("pic.png");
        user.setRole(UserRole.USER);
        user.setCreatedOn(LocalDateTime.now().minusDays(10));
        user.setUpdatedOn(LocalDateTime.now());

        when(userService.getById(userId)).thenReturn(user);

        Car car1 = new Car();
        car1.setId(UUID.randomUUID());
        car1.setBrand("BMW");
        car1.setModel("M3");
        car1.setYear(2020);
        car1.setVin("VIN1");
        car1.setJoinedAt(LocalDateTime.now().minusDays(5));

        Car car2 = new Car();
        car2.setId(UUID.randomUUID());
        car2.setBrand("Audi");
        car2.setModel("A4");
        car2.setYear(2018);
        car2.setVin("VIN2");
        car2.setJoinedAt(LocalDateTime.now().minusDays(20));

        when(carService.getCarsForUser(user)).thenReturn(List.of(car1, car2));
        when(maintenanceService.countForCar(car1.getId())).thenReturn(3);
        when(maintenanceService.countForCar(car2.getId())).thenReturn(1);

        Maintenance m1 = new Maintenance();
        m1.setId(UUID.randomUUID());
        m1.setCar(car1);
        m1.setDate(LocalDate.now());
        m1.setType(MaintenanceType.BRAKE_SERVICE);
        m1.setMileage(10000);
        m1.setDescription("Pads");
        m1.setCost(BigDecimal.TEN);

        Maintenance m2 = new Maintenance();
        m2.setId(UUID.randomUUID());
        m2.setCar(car2);
        m2.setDate(LocalDate.now().minusDays(2));
        m2.setType(MaintenanceType.OIL_CHANGE);
        m2.setMileage(20000);
        m2.setDescription("Oil");
        m2.setCost(BigDecimal.valueOf(20));

        when(maintenanceService.listForUser(user)).thenReturn(List.of(m1, m2));

        UserProfileData data = avatarPdfService.buildUserProfileData(userId);

        assertNotNull(data);
        assertEquals(2, data.getTotalCars());
        assertEquals(2, data.getTotalMaintenances());
        assertEquals(new BigDecimal("30"), data.getTotalMaintenanceCost());
        assertNotNull(data.getUserInfo());
        assertEquals("john", data.getUserInfo().getUsername());
        assertEquals("john@mail.com", data.getUserInfo().getEmail());
        assertEquals("USER", data.getUserInfo().getRole());
        assertEquals(2, data.getCars().size());
        assertEquals(2, data.getMaintenances().size());
        assertNotNull(data.getGeneratedAt());
    }

    @Test
    void buildUserProfileData_withMaintenanceWithoutCar_shouldHandleNullCar() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setProfilePictureUrl("pic.png");
        user.setRole(UserRole.USER);
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());

        when(userService.getById(userId)).thenReturn(user);
        when(carService.getCarsForUser(user)).thenReturn(List.of());

        Maintenance m1 = new Maintenance();
        m1.setId(UUID.randomUUID());
        m1.setCar(null);
        m1.setDate(LocalDate.now());
        m1.setType(MaintenanceType.OIL_CHANGE);
        m1.setMileage(10000);
        m1.setCost(BigDecimal.TEN);

        when(maintenanceService.listForUser(user)).thenReturn(List.of(m1));

        UserProfileData data = avatarPdfService.buildUserProfileData(userId);

        assertNotNull(data);
        assertEquals(0, data.getTotalCars());
        assertEquals(1, data.getTotalMaintenances());
        assertEquals(BigDecimal.TEN, data.getTotalMaintenanceCost());
        assertEquals(1, data.getMaintenances().size());
        assertEquals("N/A", data.getMaintenances().get(0).getCarBrand());
        assertEquals("N/A", data.getMaintenances().get(0).getCarModel());
    }

    @Test
    void buildUserProfileData_withMaintenanceWithoutCost_shouldCalculateCorrectly() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(UserRole.USER);
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());

        when(userService.getById(userId)).thenReturn(user);
        when(carService.getCarsForUser(user)).thenReturn(List.of());
        when(maintenanceService.listForUser(user)).thenReturn(List.of());

        UserProfileData data = avatarPdfService.buildUserProfileData(userId);

        assertNotNull(data);
        assertEquals(BigDecimal.ZERO, data.getTotalMaintenanceCost());
    }

    @Test
    void buildUserProfileData_withNullMaintenanceType_shouldHandleCorrectly() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(UserRole.USER);
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());

        when(userService.getById(userId)).thenReturn(user);
        when(carService.getCarsForUser(user)).thenReturn(List.of());

        Maintenance m1 = new Maintenance();
        m1.setId(UUID.randomUUID());
        m1.setCar(null);
        m1.setDate(LocalDate.now());
        m1.setType(null);
        m1.setMileage(10000);

        when(maintenanceService.listForUser(user)).thenReturn(List.of(m1));

        UserProfileData data = avatarPdfService.buildUserProfileData(userId);

        assertNotNull(data);
        assertEquals("N/A", data.getMaintenances().get(0).getType());
    }
}

