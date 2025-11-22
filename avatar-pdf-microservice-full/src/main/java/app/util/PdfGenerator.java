package com.example.avatarpdf.util;

import com.example.avatarpdf.exception.PdfGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PdfGenerator {

    private PdfGenerator() {}

    public static byte[] generateFromImageUrl(String imageUrl, String displayName) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            byte[] imageBytes = in.readAllBytes();
            return generate(imageBytes, displayName);
        } catch (IOException e) {
            throw new PdfGenerationException("Cannot download image from imageUrl.");
        }
    }

    private static byte[] generate(byte[] imageBytes, String displayName) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new PdfGenerationException("Image data is empty.");
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDImageXObject image = PDImageXObject.createFromByteArray(doc, imageBytes, "avatar");

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            float imgSize = 220f;
            float x = (pageWidth - imgSize) / 2;
            float y = (pageHeight - imgSize) / 2 + 60;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(image, x, y, imgSize, imgSize);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                String text = (displayName == null || displayName.isBlank()) ? "User avatar" : displayName;
                cs.newLineAtOffset(72, y - 30);
                cs.showText(text);
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to generate PDF.", e);
        }
    }
}
