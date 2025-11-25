package app.web.mapper;

import app.domain.AvatarPdf;
import app.web.dto.AvatarPdfResponse;
import app.web.dto.UserProfileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AvatarPdfMapper {

    private final ObjectMapper objectMapper;

    public AvatarPdfMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AvatarPdfResponse toResponse(AvatarPdf avatarPdf) {
        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setId(avatarPdf.getId());
        response.setDisplayName(avatarPdf.getDisplayName());
        response.setUserId(avatarPdf.getUserId());
        response.setUsername(avatarPdf.getUsername());
        response.setEmail(avatarPdf.getEmail());
        response.setRole(avatarPdf.getRole());
        response.setTotalCars(avatarPdf.getTotalCars());
        response.setTotalMaintenances(avatarPdf.getTotalMaintenances());
        response.setTotalMaintenanceCost(avatarPdf.getTotalMaintenanceCost());
        response.setGeneratedAt(avatarPdf.getGeneratedAt());

        if (avatarPdf.getUserProfileDataJson() != null && !avatarPdf.getUserProfileDataJson().isBlank()) {
            try {
                UserProfileData userProfileData = objectMapper.readValue(
                    avatarPdf.getUserProfileDataJson(), 
                    UserProfileData.class
                );
                response.setUserProfileData(userProfileData);
            } catch (Exception e) {
            }
        }

        return response;
    }
}
