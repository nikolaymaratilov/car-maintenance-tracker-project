package app.avatarPdf;

import app.avatarPdf.dto.AvatarPdfResponse;
import app.avatarPdf.dto.UserProfileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarPdfService {

    private final AvatarPdfClient client;
    private final ObjectMapper objectMapper;

    public byte[] generatePdf(MultipartFile file, String displayName) {
        return client.createPdf(file, displayName);
    }

    public byte[] generatePdfWithProfile(MultipartFile file, String displayName, UserProfileData userProfileData) {
        try {
            // Configure ObjectMapper to handle LocalDateTime properly
            String userProfileDataJson = objectMapper.writeValueAsString(userProfileData);
            return client.createPdfWithProfile(file, displayName, userProfileDataJson);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user profile data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF with profile: " + e.getMessage(), e);
        }
    }

    public AvatarPdfResponse getPdf(UUID id) {
        return client.getPdf(id);
    }

    public AvatarPdfResponse getLatestPdfForUser(UUID userId) {
        return client.getLatestPdfForUser(userId);
    }

    public java.util.List<AvatarPdfResponse> getAllPdfsForUser(UUID userId) {
        return client.getAllPdfsForUser(userId);
    }

    public void deletePdf(UUID id) {
        client.deletePdf(id);
    }

    public void deleteLatestPdfForUser(UUID userId) {
        client.deleteLatestPdfForUserPost(userId);
    }

}
