package app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class AvatarPdf {

    @Id
    @GeneratedValue
    private UUID id;

    private String displayName;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] pdfBytes;

    // User Information
    private UUID userId;
    private String username;
    private String email;
    private String profilePictureUrl;
    private String role;
    private LocalDateTime userCreatedOn;
    private LocalDateTime userUpdatedOn;

    // Statistics
    private int totalCars;
    private int totalMaintenances;
    private java.math.BigDecimal totalMaintenanceCost;

    // JSON stored user profile data (full details)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String userProfileDataJson;

    @Column(nullable = true)
    private LocalDateTime generatedAt;

    public AvatarPdf() {}

    public AvatarPdf(String displayName, byte[] pdfBytes) {
        this.displayName = displayName;
        this.pdfBytes = pdfBytes;
        this.generatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getPdfBytes() {
        return pdfBytes;
    }

    public void setPdfBytes(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getUserCreatedOn() {
        return userCreatedOn;
    }

    public void setUserCreatedOn(LocalDateTime userCreatedOn) {
        this.userCreatedOn = userCreatedOn;
    }

    public LocalDateTime getUserUpdatedOn() {
        return userUpdatedOn;
    }

    public void setUserUpdatedOn(LocalDateTime userUpdatedOn) {
        this.userUpdatedOn = userUpdatedOn;
    }

    public int getTotalCars() {
        return totalCars;
    }

    public void setTotalCars(int totalCars) {
        this.totalCars = totalCars;
    }

    public int getTotalMaintenances() {
        return totalMaintenances;
    }

    public void setTotalMaintenances(int totalMaintenances) {
        this.totalMaintenances = totalMaintenances;
    }

    public java.math.BigDecimal getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(java.math.BigDecimal totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public String getUserProfileDataJson() {
        return userProfileDataJson;
    }

    public void setUserProfileDataJson(String userProfileDataJson) {
        this.userProfileDataJson = userProfileDataJson;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
