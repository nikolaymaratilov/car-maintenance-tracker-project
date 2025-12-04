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

    private UUID userId;
    private String username;
    private String email;
    private String profilePictureUrl;
    private String role;
    private LocalDateTime userCreatedOn;
    private LocalDateTime userUpdatedOn;

    private int totalCars;
    private int totalMaintenances;
    private java.math.BigDecimal totalMaintenanceCost;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String userProfileDataJson;

    @Column(nullable = true)
    private LocalDateTime generatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AvatarPdf() {}

    public AvatarPdf(String displayName, byte[] pdfBytes) {
        this.displayName = displayName;
        this.pdfBytes = pdfBytes;
        this.generatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
    public byte[] getPdfBytes() {
        return pdfBytes;
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
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public void setUserCreatedOn(LocalDateTime userCreatedOn) {
        this.userCreatedOn = userCreatedOn;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
