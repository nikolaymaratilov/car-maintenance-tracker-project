package app.util;

import app.web.dto.CarInfo;
import app.web.dto.MaintenanceInfo;
import app.web.dto.UserInfo;
import app.web.dto.UserProfileData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfGeneratorTest {

    @Test
    void createPdf_withNullImage_shouldThrow() {
        assertThatThrownBy(() -> PdfGenerator.createPdf(null, "Test"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void createPdf_withEmptyImage_shouldThrow() {
        assertThatThrownBy(() -> PdfGenerator.createPdf(new byte[0], "Test"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void createPdfWithUserData_withValidData_shouldReturnPdfBytes() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = createUserProfileData();

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullImage_shouldCreatePdf() throws Exception {
        UserProfileData userProfileData = createUserProfileData();

        byte[] result = PdfGenerator.createPdfWithUserData(null, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withEmptyImage_shouldCreatePdf() throws Exception {
        UserProfileData userProfileData = createUserProfileData();

        byte[] result = PdfGenerator.createPdfWithUserData(new byte[0], userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullUserInfo_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withEmptyLists_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(0);
        userProfileData.setTotalMaintenances(0);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withFullData_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = createFullUserProfileData();

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullTotalMaintenanceCost_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(null);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullCarsList_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(0);
        userProfileData.setTotalMaintenances(0);
        userProfileData.setCars(null);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullMaintenancesList_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(0);
        userProfileData.setTotalMaintenances(0);
        userProfileData.setMaintenances(null);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void createPdfWithUserData_withNullGeneratedAt_shouldCreatePdf() throws Exception {
        byte[] imageBytes = createFakeImageBytes();
        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setGeneratedAt(null);

        byte[] result = PdfGenerator.createPdfWithUserData(imageBytes, userProfileData);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    private byte[] createFakeImageBytes() {
        return new byte[]{ 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private UserProfileData createUserProfileData() {
        UserProfileData data = new UserProfileData();
        data.setTotalCars(2);
        data.setTotalMaintenances(5);
        data.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));
        return data;
    }

    private UserProfileData createFullUserProfileData() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(java.util.UUID.randomUUID());
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("USER");
        userInfo.setCreatedOn(LocalDateTime.now());

        CarInfo car1 = new CarInfo();
        car1.setBrand("Toyota");
        car1.setModel("Camry");
        car1.setYear(2020);
        car1.setVin("ABC123");
        car1.setMaintenanceCount(3);
        car1.setJoinedAt(LocalDateTime.now().minusMonths(6));

        CarInfo car2 = new CarInfo();
        car2.setBrand("Honda");
        car2.setModel("Civic");
        car2.setYear(2019);
        car2.setVin("XYZ789");
        car2.setMaintenanceCount(2);
        car2.setJoinedAt(LocalDateTime.now().minusMonths(12));

        MaintenanceInfo maint1 = new MaintenanceInfo();
        maint1.setCarBrand("Toyota");
        maint1.setCarModel("Camry");
        maint1.setType("Oil Change");
        maint1.setMileage(50000);
        maint1.setCost(BigDecimal.valueOf(50.00));
        maint1.setDescription("Regular oil change");

        UserProfileData data = new UserProfileData();
        data.setUserInfo(userInfo);
        data.setCars(List.of(car1, car2));
        data.setMaintenances(List.of(maint1));
        data.setTotalCars(2);
        data.setTotalMaintenances(1);
        data.setTotalMaintenanceCost(BigDecimal.valueOf(50.00));
        data.setGeneratedAt(LocalDateTime.now());

        return data;
    }
}

