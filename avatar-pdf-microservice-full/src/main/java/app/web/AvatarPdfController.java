package app.web;

import app.service.AvatarPdfService;
import app.web.dto.AvatarPdfResponse;
import app.web.mapper.AvatarPdfMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/avatar-pdfs")
public class AvatarPdfController {

    private final AvatarPdfService service;
    private final AvatarPdfMapper mapper;

    public AvatarPdfController(AvatarPdfService service, AvatarPdfMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> createPdfFromUpload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "displayName", required = false) String displayName) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=avatar.pdf")
                .body(service.createFromUpload(file, displayName).getPdfBytes());
    }

    @PostMapping(
            value = "/upload-with-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> createPdfFromUploadWithProfile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "displayName", required = false) String displayName,
            @RequestPart("userProfileData") String userProfileDataJson) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-profile.pdf")
                .body(service.createFromUploadWithUserDataJson(file, displayName, userProfileDataJson).getPdfBytes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvatarPdfResponse> getPdf(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(service.getById(id)));
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<AvatarPdfResponse> getLatestPdfForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(mapper.toResponse(service.getLatestByUserId(userId)));
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<AvatarPdfResponse>> getAllPdfsForUser(@PathVariable UUID userId) {
        List<AvatarPdfResponse> responses = service.getAllByUserId(userId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePdf(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/latest")
    public ResponseEntity<Void> deleteLatestPdfForUser(@PathVariable UUID userId) {
        service.deleteLatestByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{userId}/latest/delete")
    public ResponseEntity<Void> deleteLatestPdfForUserPost(@PathVariable UUID userId) {
        service.deleteLatestByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
