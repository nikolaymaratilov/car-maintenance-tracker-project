package app.car;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.car.service.CarService;
import app.exception.CarCreateException;
import app.exception.CarUpdateException;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarServiceTest {

    private CarRepository carRepository;
    private CarService carService;

    @BeforeEach
    void setup() {
        carRepository = mock(CarRepository.class);
        carService = new CarService(carRepository);
    }

    @Test
    void getCarsForUser_shouldReturnCars() {
        User user = new User();
        user.setId(UUID.randomUUID());

        List<Car> cars = List.of(new Car(), new Car());
        when(carRepository.findAllByUserIdAndDeletedFalse(user.getId())).thenReturn(cars);

        List<Car> result = carService.getCarsForUser(user);

        assertEquals(2, result.size());
    }

    @Test
    void getLatestAdditions_shouldCountCarsInLast30Days() {
        Car oldCar = new Car();
        oldCar.setJoinedAt(LocalDateTime.now().minusDays(40));

        Car recentCar = new Car();
        recentCar.setJoinedAt(LocalDateTime.now().minusDays(5));

        int count = carService.getLatestAdditions(List.of(oldCar, recentCar));

        assertEquals(1, count);
    }

    @Test
    void createCar_shouldSaveCar() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Car car = new Car();
        car.setBrand("BMW");
        car.setModel("M3");
        car.setVin("123");

        carService.createCar(car, user);

        assertEquals(user, car.getUser());
        verify(carRepository).save(car);
    }

    @Test
    void createCar_shouldThrowWhenMissingFields() {
        User user = new User();
        Car car = new Car();
        car.setBrand("");
        car.setModel("");
        car.setVin("");

        assertThrows(CarCreateException.class, () -> carService.createCar(car, user));
    }

    @Test
    void filterCars_shouldReturnMatchingCars() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Car c1 = new Car();
        c1.setBrand("BMW");
        c1.setModel("M3");
        c1.setYear(2020);
        c1.setVin("ABC");

        Car c2 = new Car();
        c2.setBrand("Audi");
        c2.setModel("A4");
        c2.setYear(2018);
        c2.setVin("XYZ");

        when(carRepository.findAllByUserIdAndDeletedFalse(user.getId()))
                .thenReturn(List.of(c1, c2));

        List<Car> result = carService.filterCars(user, "BMW", null, null);

        assertEquals(1, result.size());
        assertEquals("BMW", result.get(0).getBrand());
    }

    @Test
    void deleteCar_shouldMarkAsDeleted() {
        User user = new User();
        user.setId(UUID.randomUUID());

        UUID carId = UUID.randomUUID();

        Car car = new Car();
        car.setDeleted(false);

        when(carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId()))
                .thenReturn(Optional.of(car));

        carService.deleteCar(carId, user);

        assertTrue(car.isDeleted());
        verify(carRepository).save(car);
    }

    @Test
    void getCarForUser_shouldReturnCar() {
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID carId = UUID.randomUUID();

        Car car = new Car();

        when(carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId()))
                .thenReturn(Optional.of(car));

        Car result = carService.getCarForUser(carId, user);

        assertEquals(car, result);
    }

    @Test
    void getCarForUser_shouldThrowWhenMissing() {
        User user = new User();
        user.setId(UUID.randomUUID());

        UUID carId = UUID.randomUUID();

        when(carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> carService.getCarForUser(carId, user));
    }

    @Test
    void updateCar_shouldUpdateFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID carId = UUID.randomUUID();

        Car existing = new Car();
        existing.setBrand("Old");
        existing.setModel("Old");
        existing.setVin("Old");

        Car updated = new Car();
        updated.setBrand("New");
        updated.setModel("New");
        updated.setVin("New");
        updated.setYear(2020);

        when(carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId()))
                .thenReturn(Optional.of(existing));

        carService.updateCar(carId, user, updated);

        assertEquals("New", existing.getBrand());
        assertEquals("New", existing.getModel());
        assertEquals("New", existing.getVin());
        assertEquals(2020, existing.getYear());
    }

    @Test
    void updateCar_shouldThrowWhenInvalid() {
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID carId = UUID.randomUUID();

        Car updated = new Car();
        updated.setBrand("");
        updated.setModel("");
        updated.setVin("");

        when(carRepository.findByIdAndUserIdAndDeletedFalse(carId, user.getId()))
                .thenReturn(Optional.of(new Car()));

        assertThrows(CarUpdateException.class, () -> carService.updateCar(carId, user, updated));
    }

    @Test
    void getBrandModelsMap_shouldReturnMapping() {
        Car c1 = new Car();
        c1.setBrand("BMW");
        c1.setModel("M3");

        Car c2 = new Car();
        c2.setBrand("BMW");
        c2.setModel("X5");

        Car c3 = new Car();
        c3.setBrand("Audi");
        c3.setModel("A4");

        Map<String, Set<String>> result =
                carService.getBrandModelsMap(List.of(c1, c2, c3));

        assertEquals(2, result.get("BMW").size());
        assertTrue(result.get("BMW").contains("M3"));
        assertTrue(result.get("BMW").contains("X5"));
        assertEquals(1, result.get("Audi").size());
    }

    @Test
    void getMaintenanceCountsMap_shouldReturnCounts() {
        MaintenanceService ms = mock(MaintenanceService.class);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Car c1 = new Car(); c1.setId(id1);
        Car c2 = new Car(); c2.setId(id2);

        when(ms.countForCar(id1)).thenReturn(3);
        when(ms.countForCar(id2)).thenReturn(5);

        Map<UUID, Integer> result =
                carService.getMaintenanceCountsMap(List.of(c1, c2), ms);

        assertEquals(3, result.get(id1));
        assertEquals(5, result.get(id2));
    }

    @Test
    void getAll_shouldReturnAll() {
        List<Car> cars = List.of(new Car(), new Car());

        when(carRepository.findAll()).thenReturn(cars);

        assertEquals(2, carService.getAll().size());
    }

    @Test
    void clearAllCarCache_shouldNotThrow() {
        assertDoesNotThrow(() -> carService.clearAllCarCache());
    }
}
