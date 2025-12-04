package app.service;

import app.domain.AvatarPdf;
import app.exception.ResourceNotFoundException;
import app.repository.AvatarPdfRepository;
import app.service.AvatarPdfService;
import app.web.dto.UserProfileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AvatarPdfServiceIntegrationTest {

    @Autowired
    private AvatarPdfService service;

    @Autowired
    private AvatarPdfRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createFromUpload_shouldPersistToDatabase() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        AvatarPdf saved = service.createFromUpload(file, "UserPDF");

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDisplayName()).isEqualTo("UserPDF");
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void createFromUploadWithUserData_shouldPersistWithUserData() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));

        AvatarPdf saved = service.createFromUploadWithUserData(file, "UserPDF", userProfileData);

        assertThat(saved).isNotNull();
        assertThat(saved.getTotalCars()).isEqualTo(2);
        assertThat(saved.getTotalMaintenances()).isEqualTo(5);
        assertThat(saved.getTotalMaintenanceCost()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void createFromUploadWithUserDataJson_shouldPersistWithUserData() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        String json = "{\"totalCars\":3,\"totalMaintenances\":7}";

        AvatarPdf saved = service.createFromUploadWithUserDataJson(file, "UserPDF", json);

        assertThat(saved).isNotNull();
        assertThat(saved.getTotalCars()).isEqualTo(3);
        assertThat(saved.getTotalMaintenances()).isEqualTo(7);
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void getById_shouldReturnPdf() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());
        AvatarPdf saved = service.createFromUpload(file, "TestPDF");

        AvatarPdf retrieved = service.getById(saved.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getDisplayName()).isEqualTo("TestPDF");
    }

    @Test
    void getLatestByUserId_shouldReturnLatestPdf() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        AvatarPdf pdf1 = service.createFromUploadWithUserData(file, "PDF1", new UserProfileData());
        pdf1.setUserId(userId);
        pdf1.setGeneratedAt(LocalDateTime.now().minusDays(2));
        repository.save(pdf1);

        AvatarPdf pdf2 = service.createFromUploadWithUserData(file, "PDF2", new UserProfileData());
        pdf2.setUserId(userId);
        pdf2.setGeneratedAt(LocalDateTime.now());
        repository.save(pdf2);

        AvatarPdf latest = service.getLatestByUserId(userId);

        assertThat(latest).isNotNull();
        assertThat(latest.getDisplayName()).isEqualTo("PDF2");
    }

    @Test
    void getLatestByUserId_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.getLatestByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllByUserId_shouldReturnAllPdfs() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        AvatarPdf pdf1 = service.createFromUploadWithUserData(file, "PDF1", new UserProfileData());
        pdf1.setUserId(userId);
        repository.save(pdf1);

        AvatarPdf pdf2 = service.createFromUploadWithUserData(file, "PDF2", new UserProfileData());
        pdf2.setUserId(userId);
        repository.save(pdf2);

        List<AvatarPdf> allPdfs = service.getAllByUserId(userId);

        assertThat(allPdfs).hasSize(2);
    }

    @Test
    void getAllByUserId_whenNoPdfs_shouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        List<AvatarPdf> allPdfs = service.getAllByUserId(userId);

        assertThat(allPdfs).isEmpty();
    }

    @Test
    void delete_shouldRemovePdf() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());
        AvatarPdf saved = service.createFromUpload(file, "TestPDF");

        service.delete(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> service.delete(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AvatarPdf not found");
    }

    @Test
    void deleteLatestByUserId_shouldRemoveLatestPdf() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        AvatarPdf pdf = service.createFromUploadWithUserData(file, "PDF1", new UserProfileData());
        pdf.setUserId(userId);
        repository.save(pdf);

        service.deleteLatestByUserId(userId);

        assertThat(repository.findById(pdf.getId())).isEmpty();
    }

    @Test
    void deleteLatestByUserId_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.deleteLatestByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_whenNotFound_shouldThrow() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> service.getById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AvatarPdf not found");
    }
}

