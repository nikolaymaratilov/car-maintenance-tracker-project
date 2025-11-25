package app.service;

import app.domain.AvatarPdf;
import app.exception.PdfGenerationException;
import app.exception.ResourceNotFoundException;
import app.repository.AvatarPdfRepository;
import app.util.PdfGenerator;
import app.web.dto.UserProfileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarPdfService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarPdfService.class);
    
    private final AvatarPdfRepository repository;
    private final ObjectMapper objectMapper;

    public AvatarPdf createFromUpload(MultipartFile file, String displayName) {
        try {
            logger.info("Creating PDF from upload. File name: {}, size: {}, displayName: {}", 
                file.getOriginalFilename(), file.getSize(), displayName);
            
            if (file == null || file.isEmpty()) {
                logger.error("File is null or empty");
                throw new PdfGenerationException("File is null or empty");
            }
            
            byte[] fileBytes = file.getBytes();
            logger.info("File bytes read: {} bytes", fileBytes.length);
            
            byte[] pdfBytes = PdfGenerator.createPdf(fileBytes, displayName);
            logger.info("PDF generated: {} bytes", pdfBytes.length);

            AvatarPdf avatarPdf = new AvatarPdf(displayName, pdfBytes);
            logger.info("AvatarPdf entity created, saving to database...");
            
            AvatarPdf saved = repository.save(avatarPdf);
            logger.info("AvatarPdf saved successfully with ID: {}", saved.getId());
            
            return saved;

        } catch (IOException e) {
            logger.error("IOException while processing file", e);
            throw new PdfGenerationException("Failed to read or process uploaded file.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while creating PDF", e);
            throw new PdfGenerationException("Unexpected error: " + e.getMessage(), e);
        }
    }

    public AvatarPdf createFromUploadWithUserData(MultipartFile file, String displayName, UserProfileData userProfileData) {
        try {
            if (file == null || file.isEmpty()) {
                throw new PdfGenerationException("File is null or empty");
            }

            String userProfileDataJson = objectMapper.writeValueAsString(userProfileData);

            byte[] pdfBytes = PdfGenerator.createPdfWithUserData(
                    file.getBytes(),
                    userProfileData
            );

            AvatarPdf avatarPdf = new AvatarPdf(displayName, pdfBytes);
            
            if (userProfileData.getUserInfo() != null) {
                avatarPdf.setUserId(userProfileData.getUserInfo().getUserId());
                avatarPdf.setUsername(userProfileData.getUserInfo().getUsername());
                avatarPdf.setEmail(userProfileData.getUserInfo().getEmail());
                avatarPdf.setProfilePictureUrl(userProfileData.getUserInfo().getProfilePictureUrl());
                avatarPdf.setRole(userProfileData.getUserInfo().getRole());
                avatarPdf.setUserCreatedOn(userProfileData.getUserInfo().getCreatedOn());
                avatarPdf.setUserUpdatedOn(userProfileData.getUserInfo().getUpdatedOn());
            }

            avatarPdf.setTotalCars(userProfileData.getTotalCars());
            avatarPdf.setTotalMaintenances(userProfileData.getTotalMaintenances());
            avatarPdf.setTotalMaintenanceCost(userProfileData.getTotalMaintenanceCost());
            
            avatarPdf.setUserProfileDataJson(userProfileDataJson);
            avatarPdf.setGeneratedAt(LocalDateTime.now());

            return repository.save(avatarPdf);

        } catch (IOException e) {
            throw new PdfGenerationException("Failed to read or process uploaded file.", e);
        }
    }

    public AvatarPdf createFromUploadWithUserDataJson(MultipartFile file, String displayName, String userProfileDataJson) {
        try {
            if (file == null || file.isEmpty()) {
                throw new PdfGenerationException("File is null or empty");
            }

            UserProfileData userProfileData = objectMapper.readValue(userProfileDataJson, UserProfileData.class);
            return createFromUploadWithUserData(file, displayName, userProfileData);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new PdfGenerationException("Failed to parse user profile data JSON: " + e.getMessage(), e);
        }
    }

    public AvatarPdf getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AvatarPdf not found: " + id));
    }

    public AvatarPdf getLatestByUserId(UUID userId) {
        return repository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No PDF found for user: " + userId));
    }

    public List<AvatarPdf> getAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByGeneratedAtDesc(userId);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("AvatarPdf not found: " + id);
        }
        repository.deleteById(id);
    }

    public void deleteLatestByUserId(UUID userId) {
        AvatarPdf latest = repository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No PDF found for user: " + userId));
        repository.deleteById(latest.getId());
        logger.info("Deleted latest PDF with ID: {} for user: {}", latest.getId(), userId);
    }
}
