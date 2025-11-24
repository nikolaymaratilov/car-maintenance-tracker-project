package app.web.dto;

public class AvatarPdfResponse {

    private final String displayName;
    private final byte[] pdfBytes;

    public AvatarPdfResponse(String displayName, byte[] pdfBytes) {
        this.displayName = displayName;
        this.pdfBytes = pdfBytes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public byte[] getPdfBytes() {
        return pdfBytes;
    }
}
