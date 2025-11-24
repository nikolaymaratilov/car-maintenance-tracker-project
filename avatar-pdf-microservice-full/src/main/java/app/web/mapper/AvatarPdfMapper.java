package app.web.mapper;

import app.domain.AvatarPdf;
import app.web.dto.AvatarPdfResponse;
import org.springframework.stereotype.Component;

@Component
public class AvatarPdfMapper {

    public AvatarPdfResponse toResponse(AvatarPdf pdf) {
        return new AvatarPdfResponse(pdf.getDisplayName(), pdf.getPdfBytes());
    }
}
