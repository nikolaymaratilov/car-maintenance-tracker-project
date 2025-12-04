package app.service;

import app.domain.AvatarPdf;
import app.exception.PdfGenerationException;
import app.exception.ResourceNotFoundException;
import app.repository.AvatarPdfRepository;
import app.util.PdfGenerator;
import app.web.dto.UserProfileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarPdfService {
    
    private final AvatarPdfRepository repository;
    private final ObjectMapper objectMapper;

    public AvatarPdf createFromUpload(MultipartFile file, String displayName) {
        log.info("Started creating PDF from upload");
        try {
            if (file == null || file.isEmpty()) {
                log.error("File is null or empty");
                throw new PdfGenerationException("File is null or empty");
            }
            
            byte[] fileBytes = file.getBytes();
            byte[] pdfBytes = PdfGenerator.createPdf(fileBytes, displayName);

            AvatarPdf avatarPdf = new AvatarPdf(displayName, pdfBytes);
            AvatarPdf saved = repository.save(avatarPdf);
            log.info("Successfully created PDF with ID: {}", saved.getId());
            
            return saved;

        } catch (IOException e) {
            log.error("IOException while processing file", e);
            throw new PdfGenerationException("Failed to read or process uploaded file.", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating PDF", e);
            throw new PdfGenerationException("Unexpected error: " + e.getMessage(), e);
        }
    }

    public AvatarPdf createFromUploadWithUserData(MultipartFile file, String displayName, UserProfileData userProfileData) {
        log.info("Started creating PDF with user profile data");
        try {
            if (file == null || file.isEmpty()) {
                log.error("File is null or empty");
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
            avatarPdf.setCreatedAt(LocalDateTime.now());

            AvatarPdf saved = repository.save(avatarPdf);
            log.info("Successfully created PDF with user profile data, ID: {}", saved.getId());
            return saved;

        } catch (IOException e) {
            log.error("IOException while processing file with user data", e);
            throw new PdfGenerationException("Failed to read or process uploaded file.", e);
        }
    }

    public AvatarPdf createFromUploadWithUserDataJson(MultipartFile file, String displayName, String userProfileDataJson) {
        log.info("Started creating PDF from upload with user profile JSON");
        try {
            if (file == null || file.isEmpty()) {
                log.error("File is null or empty");
                throw new PdfGenerationException("File is null or empty");
            }

            UserProfileData userProfileData = objectMapper.readValue(userProfileDataJson, UserProfileData.class);
            return createFromUploadWithUserData(file, displayName, userProfileData);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse user profile data JSON", e);
            throw new PdfGenerationException("Failed to parse user profile data JSON: " + e.getMessage(), e);
        }
    }

    public AvatarPdf getById(UUID id) {
        log.info("Retrieving PDF by ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("AvatarPdf not found: " + id));
    }

    public AvatarPdf getLatestByUserId(UUID userId) {
        log.info("Retrieving latest PDF for user: {}", userId);
        return repository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No PDF found for user: " + userId));
    }

    public List<AvatarPdf> getAllByUserId(UUID userId) {
        log.info("Retrieving all PDFs for user: {}", userId);
        return repository.findAllByUserIdOrderByGeneratedAtDesc(userId);
    }

    public void delete(UUID id) {
        log.info("Deleting PDF with ID: {}", id);
        if (!repository.existsById(id)) {
            log.error("PDF not found for deletion: {}", id);
            throw new RuntimeException("AvatarPdf not found: " + id);
        }
        repository.deleteById(id);
        log.info("Successfully deleted PDF with ID: {}", id);
    }

    public void deleteLatestByUserId(UUID userId) {
        log.info("Deleting latest PDF for user: {}", userId);
        AvatarPdf latest = repository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No PDF found for user: " + userId));
        repository.deleteById(latest.getId());
        log.info("Successfully deleted latest PDF with ID: {} for user: {}", latest.getId(), userId);
    }

    public int deleteOldPdfs() {
        log.info("Started cleanup of old PDFs");
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        List<AvatarPdf> oldPdfs = repository.findAllByCreatedAtBefore(threshold);

        repository.deleteAll(oldPdfs);
        log.info("Completed cleanup of old PDFs, deleted {} PDFs", oldPdfs.size());

        return oldPdfs.size();
    }
}
