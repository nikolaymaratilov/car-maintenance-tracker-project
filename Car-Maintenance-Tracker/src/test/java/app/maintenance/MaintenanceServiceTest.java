package app.maintenance;

import app.car.model.Car;
import app.car.repository.CarRepository;
import app.car.service.CarService;
import app.exception.MaintenanceCreateException;
import app.exception.MaintenanceUpdateException;
import app.maintenance.model.Maintenance;
import app.maintenance.model.MaintenanceType;
import app.maintenance.repository.MaintenanceRepository;
import app.maintenance.service.MaintenanceService;
import app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceServiceTest {

    private MaintenanceRepository maintenanceRepository;
    private CarRepository carRepository;
    private CarService carService;
    private MaintenanceService maintenanceService;

    @BeforeEach
    void setup() {
        maintenanceRepository = mock(MaintenanceRepository.class);
        carRepository = mock(CarRepository.class);
        carService = mock(CarService.class);
        maintenanceService = new MaintenanceService(maintenanceRepository, carRepository, carService);
    }

    @Test
    void listForCar_shouldReturnMaintenances() {
        UUID carId = UUID.randomUUID();
        List<Maintenance> list = List.of(new Maintenance(), new Maintenance());
        when(maintenanceRepository.findAllByCarIdOrderByDateDesc(carId)).thenReturn(list);

        assertEquals(2, maintenanceService.listForCar(carId).size());
    }

    @Test
    void listForUser_shouldReturnMaintenances() {
        User user = new User();
        user.setId(UUID.randomUUID());
        List<Maintenance> list = List.of(new Maintenance());
        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId())).thenReturn(list);

        assertEquals(1, maintenanceService.listForUser(user).size());
    }

    @Test
    void filterForUser_shouldFilterCorrectly() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setBrand("BMW");
        car.setModel("M3");

        Maintenance m1 = new Maintenance();
        m1.setCar(car);
        m1.setType(MaintenanceType.OIL_CHANGE);
        m1.setDescription("Changed oil");

        Maintenance m2 = new Maintenance();
        m2.setCar(car);
        m2.setType(MaintenanceType.BRAKE_SERVICE);
        m2.setDescription("break pads");

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(m1, m2));

        List<Maintenance> result = maintenanceService.filterForUser(user, car.getId(), MaintenanceType.OIL_CHANGE, "oil");

        assertEquals(1, result.size());
        assertEquals(MaintenanceType.OIL_CHANGE, result.get(0).getType());
    }

    @Test
    void upcomingForUser_shouldReturnSortedUpcoming() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Maintenance m1 = new Maintenance();
        m1.setNextDueDate(LocalDate.now().plusDays(10));

        Maintenance m2 = new Maintenance();
        m2.setNextDueDate(LocalDate.now().plusDays(5));

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(m1, m2));

        List<Maintenance> result = maintenanceService.upcomingForUser(user);

        assertEquals(2, result.size());
        assertEquals(m2, result.get(0));
    }

    @Test
    void delete_shouldCallRepository() {
        UUID carId = UUID.randomUUID();
        UUID maintenanceId = UUID.randomUUID();

        maintenanceService.delete(carId, maintenanceId);

        verify(maintenanceRepository).deleteByIdAndCarId(maintenanceId, carId);
    }

    @Test
    void createMaintenance_shouldSave() {
        Maintenance m = new Maintenance();
        m.setCar(new Car());
        m.setType(MaintenanceType.BRAKE_SERVICE);

        maintenanceService.createMaintenance(m);

        verify(maintenanceRepository).save(m);
    }

    @Test
    void createMaintenance_shouldThrowWhenInvalid() {
        Maintenance m = new Maintenance();

        assertThrows(MaintenanceCreateException.class, () -> maintenanceService.createMaintenance(m));
    }

    @Test
    void getForUser_shouldReturnMaintenance() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Car car = new Car();
        car.setUser(user);

        Maintenance m = new Maintenance();
        m.setCar(car);

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(m));

        assertEquals(m, maintenanceService.getForUser(id, user));
    }

    @Test
    void getForUser_shouldThrowWhenNotOwned() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(new Maintenance()));

        assertThrows(NoSuchElementException.class, () -> maintenanceService.getForUser(id, user));
    }

    @Test
    void countForCar_shouldReturnCount() {
        UUID id = UUID.randomUUID();
        when(maintenanceRepository.countByCarId(id)).thenReturn(5);

        assertEquals(5, maintenanceService.countForCar(id));
    }

    @Test
    void update_shouldUpdateFields() {
        UUID mId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Car existingCar = new Car();
        existingCar.setId(UUID.randomUUID());
        existingCar.setUser(user);

        Maintenance existing = new Maintenance();
        existing.setCar(existingCar);

        Car newCar = new Car();
        newCar.setId(UUID.randomUUID());
        newCar.setUser(user);

        Maintenance updated = new Maintenance();
        updated.setCar(newCar);
        updated.setType(MaintenanceType.BRAKE_SERVICE);
        updated.setDate(LocalDate.now());
        updated.setMileage(10000);
        updated.setDescription("desc");
        updated.setCost(BigDecimal.TEN);
        updated.setNextDueDate(LocalDate.now().plusDays(10));

        when(maintenanceRepository.findById(mId)).thenReturn(Optional.of(existing));
        when(carRepository.findByIdAndUserId(newCar.getId(), user.getId())).thenReturn(Optional.of(newCar));

        maintenanceService.update(mId, user, updated);

        assertEquals(newCar, existing.getCar());
        assertEquals(updated.getType(), existing.getType());
        assertEquals(updated.getMileage(), existing.getMileage());
    }


    @Test
    void update_shouldThrowWhenInvalid() {
        UUID mId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setUser(user);

        Maintenance existing = new Maintenance();
        existing.setCar(car);

        Maintenance updated = new Maintenance();

        when(maintenanceRepository.findById(mId)).thenReturn(Optional.of(existing));

        assertThrows(MaintenanceUpdateException.class, () -> maintenanceService.update(mId, user, updated));
    }

    @Test
    void upcomingNext30Days_shouldReturnCorrectList() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Maintenance m1 = new Maintenance();
        m1.setNextDueDate(LocalDate.now().plusDays(5));

        Maintenance m2 = new Maintenance();
        m2.setNextDueDate(LocalDate.now().plusDays(40));

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(m1, m2));

        List<Maintenance> result = maintenanceService.upcomingNext30Days(user);

        assertEquals(1, result.size());
    }

    @Test
    void getRecentMaintenances_shouldLimitSize() {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(new Maintenance(), new Maintenance()));

        List<Maintenance> result = maintenanceService.getRecentMaintenances(user, 1);

        assertEquals(1, result.size());
    }

    @Test
    void getMonthlyCost_shouldReturnSum() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Maintenance m1 = new Maintenance();
        m1.setDate(LocalDate.now());
        m1.setCost(BigDecimal.TEN);

        Maintenance m2 = new Maintenance();
        m2.setDate(LocalDate.now());
        m2.setCost(BigDecimal.ONE);

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(m1, m2));

        assertEquals(new BigDecimal("11"), maintenanceService.getMonthlyCost(user));
    }

    @Test
    void getTotalSpent_shouldSumCosts() {
        Maintenance m1 = new Maintenance();
        m1.setCost(BigDecimal.ONE);

        Maintenance m2 = new Maintenance();
        m2.setCost(BigDecimal.TEN);

        assertEquals(new BigDecimal("11"),
                maintenanceService.getTotalSpent(List.of(m1, m2)));
    }

    @Test
    void getThisMonthCount_shouldCountCorrect() {
        Maintenance m1 = new Maintenance();
        m1.setDate(LocalDate.now());

        Maintenance m2 = new Maintenance();
        m2.setDate(LocalDate.now().minusMonths(1));

        assertEquals(1, maintenanceService.getThisMonthCount(List.of(m1, m2)));
    }

    @Test
    void getServiceReadyCount_shouldReturnDistinctCars() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Car c1 = new Car(); c1.setId(UUID.randomUUID());
        Car c2 = new Car(); c2.setId(UUID.randomUUID());

        Maintenance m1 = new Maintenance();
        m1.setNextDueDate(LocalDate.now().plusDays(5));
        m1.setCar(c1);

        Maintenance m2 = new Maintenance();
        m2.setNextDueDate(LocalDate.now().plusDays(8));
        m2.setCar(c2);

        when(maintenanceRepository.findAllByCarUserIdOrderByDateDesc(user.getId()))
                .thenReturn(List.of(m1, m2));

        long count = maintenanceService.getServiceReadyCount(user);

        assertEquals(2, count);
    }

    @Test
    void prepareNewMaintenanceForm_shouldSetCarWhenCarIdProvided() {
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID carId = UUID.randomUUID();

        Car car = new Car();
        car.setId(carId);

        when(carService.getCarForUser(carId, user)).thenReturn(car);

        Maintenance result = maintenanceService.prepareNewMaintenanceForm(user, carId);

        assertEquals(car, result.getCar());
    }

    @Test
    void deleteMaintenanceForUser_shouldDeleteWhenOwned() {
        UUID mId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Car car = new Car();
        car.setId(UUID.randomUUID());
        car.setUser(user);

        Maintenance m = new Maintenance();
        m.setId(mId);
        m.setCar(car);

        when(maintenanceRepository.findById(mId)).thenReturn(Optional.of(m));

        maintenanceService.deleteMaintenanceForUser(mId, user);

        verify(maintenanceRepository).deleteByIdAndCarId(mId, car.getId());
    }

    @Test
    void getAll_shouldReturnList() {
        List<Maintenance> list = List.of(new Maintenance(), new Maintenance());
        when(maintenanceRepository.findAll()).thenReturn(list);

        assertEquals(2, maintenanceService.getAll().size());
    }
}
