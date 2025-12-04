package app.web.mapper;

import app.domain.AvatarPdf;
import app.web.dto.UserProfileData;
import app.web.mapper.AvatarPdfMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvatarPdfMapperTest {

    private AvatarPdfMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mapper = new AvatarPdfMapper(objectMapper);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime generatedAt = LocalDateTime.now();

        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        try {
            java.lang.reflect.Field idField = AvatarPdf.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pdf, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pdf.setUserId(userId);
        pdf.setUsername("john");
        pdf.setEmail("john@mail.com");
        pdf.setRole("USER");
        pdf.setTotalCars(2);
        pdf.setTotalMaintenances(5);
        pdf.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));
        pdf.setGeneratedAt(generatedAt);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getDisplayName()).isEqualTo("test-pdf");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getEmail()).isEqualTo("john@mail.com");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getTotalCars()).isEqualTo(2);
        assertThat(response.getTotalMaintenances()).isEqualTo(5);
        assertThat(response.getTotalMaintenanceCost()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(response.getGeneratedAt()).isEqualTo(generatedAt);
    }

    @Test
    void toResponse_withUserProfileDataJson_shouldMapUserProfileData() throws Exception {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(2);
        userProfileData.setTotalMaintenances(5);
        userProfileData.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));

        String json = objectMapper.writeValueAsString(userProfileData);
        pdf.setUserProfileDataJson(json);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNotNull();
        assertThat(response.getUserProfileData().getTotalCars()).isEqualTo(2);
        assertThat(response.getUserProfileData().getTotalMaintenances()).isEqualTo(5);
    }

    @Test
    void toResponse_withInvalidJson_shouldNotSetUserProfileData() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserProfileDataJson("invalid json");

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNull();
    }

    @Test
    void toResponse_withNullUserProfileDataJson_shouldNotSetUserProfileData() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserProfileDataJson(null);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNull();
    }

    @Test
    void toResponse_withEmptyUserProfileDataJson_shouldNotSetUserProfileData() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserProfileDataJson("");

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNull();
    }

    @Test
    void toResponse_withAllNullUserProfileDataFields_shouldMapCorrectly() throws Exception {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());

        UserProfileData userProfileData = new UserProfileData();
        userProfileData.setTotalCars(0);
        userProfileData.setTotalMaintenances(0);
        userProfileData.setTotalMaintenanceCost(null);

        String json = objectMapper.writeValueAsString(userProfileData);
        pdf.setUserProfileDataJson(json);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNotNull();
        assertThat(response.getUserProfileData().getTotalCars()).isEqualTo(0);
        assertThat(response.getUserProfileData().getTotalMaintenances()).isEqualTo(0);
    }

    @Test
    void toResponse_withWhitespaceUserProfileDataJson_shouldNotSetUserProfileData() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserProfileDataJson("   ");

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNull();
    }

    @Test
    void toResponse_withEmptyUserProfileDataJson_shouldSetUserProfileData() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(UUID.randomUUID());
        pdf.setUserProfileDataJson("{}");

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getUserProfileData()).isNotNull();
    }

    @Test
    void toResponse_withZeroValues_shouldMapCorrectly() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setTotalCars(0);
        pdf.setTotalMaintenances(0);
        pdf.setTotalMaintenanceCost(BigDecimal.ZERO);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getTotalCars()).isEqualTo(0);
        assertThat(response.getTotalMaintenances()).isEqualTo(0);
        assertThat(response.getTotalMaintenanceCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void toResponse_withNullGeneratedAt_shouldMapCorrectly() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setGeneratedAt(null);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getGeneratedAt()).isNull();
    }

    @Test
    void toResponse_withNullTotalMaintenanceCost_shouldMapCorrectly() {
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setTotalMaintenanceCost(null);

        app.web.dto.AvatarPdfResponse response = mapper.toResponse(pdf);

        assertThat(response).isNotNull();
        assertThat(response.getTotalMaintenanceCost()).isNull();
    }
}

