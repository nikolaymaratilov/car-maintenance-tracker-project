package app.service;

import app.domain.AvatarPdf;
import app.exception.PdfGenerationException;
import app.util.PdfGenerator;
import org.springframework.stereotype.Service;

@Service
public class AvatarPdfService {

    public AvatarPdf createFromUpload(byte[] imageBytes, String displayName) {
        try {
            byte[] pdf = PdfGenerator.createPdf(imageBytes, displayName);
            return new AvatarPdf(displayName, pdf);
        } catch (Exception e) {
            throw new PdfGenerationException("Failed to generate PDF from uploaded image.", e);
        }
    }
}
