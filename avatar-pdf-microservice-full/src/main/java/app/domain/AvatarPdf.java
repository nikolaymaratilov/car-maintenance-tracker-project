package app.domain;

public class AvatarPdf {

    private final String displayName;
    private final byte[] pdfBytes;

    public AvatarPdf(String displayName, byte[] pdfBytes) {
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
