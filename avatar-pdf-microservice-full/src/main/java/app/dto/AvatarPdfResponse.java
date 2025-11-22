package com.example.avatarpdf.dto;

import java.util.UUID;

public class AvatarPdfResponse {
    private UUID id;
    private UUID userId;
    private String downloadUrl;

    public AvatarPdfResponse(UUID id, UUID userId, String downloadUrl) {
        this.id = id;
        this.userId = userId;
        this.downloadUrl = downloadUrl;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDownloadUrl() { return downloadUrl; }
}
