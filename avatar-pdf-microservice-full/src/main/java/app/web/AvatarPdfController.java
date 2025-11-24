package app.web;

import app.service.AvatarPdfService;
import app.web.mapper.AvatarPdfMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/avatar-pdfs")
public class AvatarPdfController {

    private final AvatarPdfService service;
    private final AvatarPdfMapper mapper;

    public AvatarPdfController(AvatarPdfService service, AvatarPdfMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Upload image from laptop -> receive PDF bytes.
     * Feign-friendly multipart endpoint.
     */
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> createPdfFromUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName
    ) {

        try {
            var avatarPdf = service.createFromUpload(file.getBytes(), displayName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=avatar.pdf")
                    .body(avatarPdf.getPdfBytes());

        } catch (Exception e) {
            throw new app.exception.PdfGenerationException("Cannot read uploaded file.", e);
        }
    }
}
