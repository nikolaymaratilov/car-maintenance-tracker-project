package app.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfGenerator {

    private PdfGenerator() {

    }

    public static byte[] createPdf(byte[] imageBytes, String displayName) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "avatar");

            float imgSize = 220f;
            float x = 100f;
            float y = 350f;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.drawImage(image, x, y, imgSize, imgSize);

                if (displayName != null && !displayName.isBlank()) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    cs.newLineAtOffset(x, y - 30);
                    cs.showText(displayName);
                    cs.endText();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
