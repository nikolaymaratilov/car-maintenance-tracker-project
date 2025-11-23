package app.dto;

import jakarta.validation.constraints.NotBlank;

public class AvatarPdfUpdateRequest {

    @NotBlank(message = "imageUrl is required")
    private String imageUrl;

    private String displayName;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
