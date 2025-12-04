package app.service;

import app.domain.AvatarPdf;
import app.exception.PdfGenerationException;
import app.exception.ResourceNotFoundException;
import app.repository.AvatarPdfRepository;
import app.service.AvatarPdfService;
import app.util.PdfGenerator;
import app.web.dto.UserProfileData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvatarPdfServiceTest {

    private AvatarPdfService service;
    private AvatarPdfRepository repository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        repository = mock(AvatarPdfRepository.class);
        objectMapper = mock(ObjectMapper.class);
        service = new AvatarPdfService(repository, objectMapper);
    }

    @Test
    void createFromUpload_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdf(any(), anyString()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUpload(file, "Test");

            assertThat(result).isNotNull();
            assertThat(result.getDisplayName()).isEqualTo("Test");
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUpload_withEmptyFile_shouldThrow() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.createFromUpload(file, "Test"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("File is null or empty");
    }

    @Test
    void createFromUpload_withNullFile_shouldThrow() {
        assertThatThrownBy(() -> service.createFromUpload(null, "Test"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("File is null or empty");
    }

    @Test
    void createFromUpload_withIOException_shouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("IO error"));

        assertThatThrownBy(() -> service.createFromUpload(file, "Test"))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("Failed to read or process uploaded file");
    }

    @Test
    void createFromUpload_withException_shouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdf(any(), anyString()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            assertThatThrownBy(() -> service.createFromUpload(file, "Test"))
                    .isInstanceOf(PdfGenerationException.class)
                    .hasMessageContaining("Unexpected error");
        }
    }

    @Test
    void createFromUploadWithUserData_shouldSavePdfWithUserData() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));

        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, "Test", userProfileData);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserData_withEmptyFile_shouldThrow() {
        UserProfileData userProfileData = new UserProfileData();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.createFromUploadWithUserData(file, "Test", userProfileData))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("File is null or empty");
    }

    @Test
    void createFromUploadWithUserData_withNullUserInfo_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setUserInfo(null);
        userProfileData.setTotalCars(2);

        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, "Test", userProfileData);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserData_withIOException_shouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("IO error"));

        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        assertThatThrownBy(() -> service.createFromUploadWithUserData(file, "Test", userProfileData))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("Failed to read or process uploaded file");
    }

    @Test
    void createFromUploadWithUserDataJson_shouldParseJsonAndCreatePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        String json = "{\"totalCars\":2}";
        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.readValue(json, UserProfileData.class)).thenReturn(userProfileData);
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserDataJson(file, "Test", json);

            assertThat(result).isNotNull();
            verify(objectMapper).readValue(json, UserProfileData.class);
        }
    }

    @Test
    void createFromUploadWithUserDataJson_withInvalidJson_shouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        String invalidJson = "invalid json";

        when(objectMapper.readValue(invalidJson, UserProfileData.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        assertThatThrownBy(() -> service.createFromUploadWithUserDataJson(file, "Test", invalidJson))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("Failed to parse user profile data JSON");
    }

    @Test
    void getById_shouldReturnPdf() {
        UUID id = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("Test", "pdf".getBytes());
        try {
            java.lang.reflect.Field idField = AvatarPdf.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pdf, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(repository.findById(id)).thenReturn(Optional.of(pdf));

        AvatarPdf result = service.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getDisplayName()).isEqualTo("Test");
        verify(repository).findById(id);
    }

    @Test
    void getById_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AvatarPdf not found");
    }

    @Test
    void getLatestByUserId_shouldReturnLatestPdf() {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("Test", "pdf".getBytes());
        pdf.setUserId(userId);

        when(repository.findFirstByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(Optional.of(pdf));

        AvatarPdf result = service.getLatestByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void getLatestByUserId_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(repository.findFirstByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatestByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No PDF found for user");
    }

    @Test
    void getAllByUserId_shouldReturnAllPdfs() {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf1 = new AvatarPdf("Test1", "pdf1".getBytes());
        AvatarPdf pdf2 = new AvatarPdf("Test2", "pdf2".getBytes());

        when(repository.findAllByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(List.of(pdf1, pdf2));

        List<AvatarPdf> result = service.getAllByUserId(userId);

        assertThat(result).hasSize(2);
        verify(repository).findAllByUserIdOrderByGeneratedAtDesc(userId);
    }

    @Test
    void delete_shouldDeletePdf() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AvatarPdf not found");
    }

    @Test
    void deleteLatestByUserId_shouldDeleteLatestPdf() {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("Test", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserId(userId);

        when(repository.findFirstByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(Optional.of(pdf));

        service.deleteLatestByUserId(userId);

        verify(repository).deleteById(pdf.getId());
    }

    @Test
    void deleteLatestByUserId_whenNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(repository.findFirstByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteLatestByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteOldPdfs_shouldDeleteOldPdfs() {
        AvatarPdf oldPdf1 = new AvatarPdf("Old1", "pdf1".getBytes());
        AvatarPdf oldPdf2 = new AvatarPdf("Old2", "pdf2".getBytes());

        when(repository.findAllByCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(oldPdf1, oldPdf2));

        int deleted = service.deleteOldPdfs();

        assertThat(deleted).isEqualTo(2);
        verify(repository).deleteAll(List.of(oldPdf1, oldPdf2));
    }

    @Test
    void deleteOldPdfs_whenNoOldPdfs_shouldReturnZero() {
        when(repository.findAllByCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        int deleted = service.deleteOldPdfs();

        assertThat(deleted).isEqualTo(0);
        verify(repository).deleteAll(List.of());
    }

    @Test
    void createFromUploadWithUserData_withJsonProcessingException_shouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.writeValueAsString(userProfileData))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {});

        assertThatThrownBy(() -> service.createFromUploadWithUserData(file, "Test", userProfileData))
                .isInstanceOf(PdfGenerationException.class)
                .hasMessageContaining("Failed to read or process uploaded file");
    }

    @Test
    void createFromUploadWithUserData_withNullDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf(null, "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, null, userProfileData);

            assertThat(result).isNotNull();
            assertThat(result.getDisplayName()).isNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserData_withFullUserInfo_shouldSetAllFields() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        app.web.dto.UserInfo userInfo = new app.web.dto.UserInfo();
        UUID userId = UUID.randomUUID();
        userInfo.setUserId(userId);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setRole("USER");
        userInfo.setProfilePictureUrl("http://example.com/pic.jpg");
        userInfo.setCreatedOn(LocalDateTime.now());
        userInfo.setUpdatedOn(LocalDateTime.now());
        userProfileData.setUserInfo(userInfo);
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));

        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, "Test", userProfileData);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUpload_withBlankDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        AvatarPdf saved = new AvatarPdf("   ", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdf(any(), anyString()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUpload(file, "   ");

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserDataJson_withNullDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        String json = "{\"totalCars\":2}";
        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.readValue(json, UserProfileData.class)).thenReturn(userProfileData);
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf(null, "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserDataJson(file, null, json);

            assertThat(result).isNotNull();
            verify(objectMapper).readValue(json, UserProfileData.class);
        }
    }

    @Test
    void createFromUploadWithUserData_withBlankDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("   ", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, "   ", userProfileData);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserDataJson_withBlankDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        String json = "{\"totalCars\":2}";
        UserProfileData userProfileData = new UserProfileData();
        when(objectMapper.readValue(json, UserProfileData.class)).thenReturn(userProfileData);
        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("   ", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserDataJson(file, "   ", json);

            assertThat(result).isNotNull();
            verify(objectMapper).readValue(json, UserProfileData.class);
        }
    }

    @Test
    void createFromUpload_withNullDisplayName_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        AvatarPdf saved = new AvatarPdf(null, "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdf(any(), isNull()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUpload(file, null);

            assertThat(result).isNotNull();
            assertThat(result.getDisplayName()).isNull();
            verify(repository).save(any());
        }
    }

    @Test
    void createFromUploadWithUserData_withNullTotalMaintenanceCost_shouldSavePdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("img".getBytes());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(null);

        when(objectMapper.writeValueAsString(userProfileData)).thenReturn("{}");

        AvatarPdf saved = new AvatarPdf("Test", "pdf".getBytes());
        when(repository.save(any())).thenReturn(saved);

        try (MockedStatic<PdfGenerator> utilities = Mockito.mockStatic(PdfGenerator.class)) {
            utilities.when(() -> PdfGenerator.createPdfWithUserData(any(), any()))
                    .thenReturn("pdf".getBytes());

            AvatarPdf result = service.createFromUploadWithUserData(file, "Test", userProfileData);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }
    }
}

