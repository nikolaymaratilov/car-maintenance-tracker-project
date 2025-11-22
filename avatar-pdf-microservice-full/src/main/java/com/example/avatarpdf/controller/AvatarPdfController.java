package com.example.avatarpdf.controller;

import com.example.avatarpdf.dto.AvatarPdfCreateRequest;
import com.example.avatarpdf.dto.AvatarPdfResponse;
import com.example.avatarpdf.dto.AvatarPdfUpdateRequest;
import com.example.avatarpdf.entity.AvatarPdf;
import com.example.avatarpdf.service.AvatarPdfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/avatar-pdfs")
public class AvatarPdfController {

    private final AvatarPdfService service;

    public AvatarPdfController(AvatarPdfService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AvatarPdfResponse> create(@Valid @RequestBody AvatarPdfCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvatarPdfResponse> update(@PathVariable UUID id,
                                                    @RequestParam UUID userId,
                                                    @Valid @RequestBody AvatarPdfUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, userId, request));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable UUID id,
                                           @RequestParam UUID userId) {
        AvatarPdf pdf = service.getById(id, userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdf.getUserId() + "-avatar.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.getPdfContent());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam UUID userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
