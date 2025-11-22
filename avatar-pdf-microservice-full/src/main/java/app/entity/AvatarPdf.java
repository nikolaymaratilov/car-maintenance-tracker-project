package com.example.avatarpdf.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "avatar_pdfs")
public class AvatarPdf {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String imageUrl;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] pdfContent;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdOn = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public byte[] getPdfContent() { return pdfContent; }
    public void setPdfContent(byte[] pdfContent) { this.pdfContent = pdfContent; }
    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
