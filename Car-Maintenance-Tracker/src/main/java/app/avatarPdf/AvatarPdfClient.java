package app.avatarPdf;

import app.avatarPdf.dto.AvatarPdfResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@FeignClient(
        name = "avatar-pdf-svc",
        url = "${avatar.pdf.service.url}"
)
public interface AvatarPdfClient {

    @PostMapping(
            value = "/api/avatar-pdfs/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    byte[] createPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "displayName", required = false) String displayName
    );

    @PostMapping(
            value = "/api/avatar-pdfs/upload-with-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    byte[] createPdfWithProfile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "displayName", required = false) String displayName,
            @RequestPart("userProfileData") String userProfileDataJson
    );

    @GetMapping("/api/avatar-pdfs/{id}")
    AvatarPdfResponse getPdf(@PathVariable("id") UUID id);

    @GetMapping("/api/avatar-pdfs/user/{userId}/latest")
    AvatarPdfResponse getLatestPdfForUser(@PathVariable("userId") UUID userId);

    @GetMapping("/api/avatar-pdfs/user/{userId}/all")
    java.util.List<AvatarPdfResponse> getAllPdfsForUser(@PathVariable("userId") UUID userId);

    @DeleteMapping("/api/avatar-pdfs/{id}")
    void deletePdf(@PathVariable("id") UUID id);

    @DeleteMapping("/api/avatar-pdfs/user/{userId}/latest")
    void deleteLatestPdfForUser(@PathVariable("userId") UUID userId);

    @PostMapping("/api/avatar-pdfs/user/{userId}/latest/delete")
    void deleteLatestPdfForUserPost(@PathVariable("userId") UUID userId);
}

