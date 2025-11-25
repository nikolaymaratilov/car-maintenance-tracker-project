package app.util;

import app.web.dto.CarInfo;
import app.web.dto.MaintenanceInfo;
import app.web.dto.UserInfo;
import app.web.dto.UserProfileData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private static final float PAGE_WIDTH = 612f;
    private static final float PAGE_HEIGHT = 792f;
    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 20f;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private PdfGenerator() {

    }

    public static byte[] createPdf(byte[] imageBytes, String displayName) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Image bytes are null or empty");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDImageXObject image;
            try {
                image = PDImageXObject.createFromByteArray(document, imageBytes, "avatar");
            } catch (Exception e) {
                throw new IOException("Failed to create image from bytes. The file might not be a valid image format.", e);
            }

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

    public static byte[] createPdfWithUserData(byte[] imageBytes, UserProfileData userProfileData) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PageContext pageContext = new PageContext(document);
            float currentY = PAGE_HEIGHT - MARGIN;

            // Title
            currentY = writeText(pageContext, "User Profile", currentY, 
                PDType1Font.HELVETICA_BOLD, 24);

            // User Avatar
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "avatar");
                    float imgSize = 100f;
                    currentY = ensureSpace(pageContext, currentY, imgSize + 30);
                    pageContext.getCurrentStream().drawImage(image, MARGIN, currentY - imgSize, imgSize, imgSize);
                    currentY -= imgSize + 20;
                } catch (Exception e) {
                    // If image fails to load, continue without it
                }
            }

            // User Information Section
            UserInfo userInfo = userProfileData.getUserInfo();
            if (userInfo != null) {
                currentY = ensureSpace(pageContext, currentY, 100);
                currentY = addSection(pageContext, "User Information", currentY);
                currentY = addTextLine(pageContext, "Username: " + 
                    (userInfo.getUsername() != null ? userInfo.getUsername() : "N/A"), currentY);
                currentY = addTextLine(pageContext, "Email: " + 
                    (userInfo.getEmail() != null ? userInfo.getEmail() : "N/A"), currentY);
                currentY = addTextLine(pageContext, "Role: " + 
                    (userInfo.getRole() != null ? userInfo.getRole() : "N/A"), currentY);
                if (userInfo.getCreatedOn() != null) {
                    currentY = addTextLine(pageContext, "Created on: " + 
                        userInfo.getCreatedOn().format(DATETIME_FORMATTER), currentY);
                }
                currentY -= 10;
            }

            // Statistics Section
            currentY = ensureSpace(pageContext, currentY, 80);
            currentY = addSection(pageContext, "Statistics", currentY);
            currentY = addTextLine(pageContext, "Total Cars: " + userProfileData.getTotalCars(), currentY);
            currentY = addTextLine(pageContext, "Total Maintenances: " + userProfileData.getTotalMaintenances(), currentY);
            if (userProfileData.getTotalMaintenanceCost() != null) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                currentY = addTextLine(pageContext, "Total Maintenance Cost: " + 
                    currencyFormat.format(userProfileData.getTotalMaintenanceCost().doubleValue()), currentY);
            }
            currentY -= 10;

            // Cars Section
            List<CarInfo> cars = userProfileData.getCars();
            if (cars != null && !cars.isEmpty()) {
                currentY = ensureSpace(pageContext, currentY, 150);
                currentY = addSection(pageContext, "Cars (" + cars.size() + ")", currentY);
                for (CarInfo car : cars) {
                    currentY = ensureSpace(pageContext, currentY, 100);
                    currentY = addTextLine(pageContext, "• " + car.getBrand() + " " + car.getModel() + 
                        " (" + car.getYear() + ")", currentY, PDType1Font.HELVETICA_BOLD);
                    currentY = addTextLine(pageContext, "  VIN: " + car.getVin(), currentY);
                    if (car.getJoinedAt() != null) {
                        currentY = addTextLine(pageContext, "  Added on: " + 
                            car.getJoinedAt().format(DATETIME_FORMATTER), currentY);
                    }
                    currentY = addTextLine(pageContext, "  Maintenance Count: " + car.getMaintenanceCount(), currentY);
                    currentY -= 5;
                }
                currentY -= 10;
            }

            // Maintenances Section
            List<MaintenanceInfo> maintenances = userProfileData.getMaintenances();
            if (maintenances != null && !maintenances.isEmpty()) {
                currentY = ensureSpace(pageContext, currentY, 150);
                currentY = addSection(pageContext, "Maintenances (" + maintenances.size() + ")", currentY);
                for (MaintenanceInfo maintenance : maintenances) {
                    currentY = ensureSpace(pageContext, currentY, 120);
                    currentY = addTextLine(pageContext, "• " + maintenance.getCarBrand() + " " + 
                        maintenance.getCarModel(), currentY, PDType1Font.HELVETICA_BOLD);
                    currentY = addTextLine(pageContext, "  Type: " + maintenance.getType(), currentY);
                    if (maintenance.getDate() != null) {
                        currentY = addTextLine(pageContext, "  Date: " + 
                            maintenance.getDate().format(DATE_FORMATTER), currentY);
                    }
                    currentY = addTextLine(pageContext, "  Mileage: " + maintenance.getMileage() + " km", currentY);
                    if (maintenance.getCost() != null) {
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
                        currentY = addTextLine(pageContext, "  Cost: " + 
                            currencyFormat.format(maintenance.getCost().doubleValue()), currentY);
                    }
                    if (maintenance.getDescription() != null && !maintenance.getDescription().isBlank()) {
                        currentY = addTextLine(pageContext, "  Description: " + maintenance.getDescription(), currentY);
                    }
                    if (maintenance.getNextDueDate() != null) {
                        currentY = addTextLine(pageContext, "  Next Due Date: " + 
                            maintenance.getNextDueDate().format(DATE_FORMATTER), currentY);
                    }
                    currentY -= 5;
                }
            }

            // Generated timestamp
            if (userProfileData.getGeneratedAt() != null) {
                currentY = ensureSpace(pageContext, currentY, 30);
                currentY -= 20;
                PDPageContentStream cs = pageContext.getCurrentStream();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                cs.newLineAtOffset(MARGIN, currentY);
                cs.showText("Generated on: " + userProfileData.getGeneratedAt().format(DATETIME_FORMATTER));
                cs.endText();
            }

            pageContext.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private static class PageContext {
        private final PDDocument document;
        private PDPage currentPage;
        private PDPageContentStream currentStream;

        PageContext(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void newPage() throws IOException {
            if (currentStream != null) {
                currentStream.close();
            }
            currentPage = new PDPage();
            document.addPage(currentPage);
            currentStream = new PDPageContentStream(document, currentPage);
        }

        PDPageContentStream getCurrentStream() {
            return currentStream;
        }

        void close() throws IOException {
            if (currentStream != null) {
                currentStream.close();
            }
        }
    }

    private static float ensureSpace(PageContext pageContext, float currentY, float requiredSpace) throws IOException {
        if (currentY < requiredSpace) {
            pageContext.newPage();
            return PAGE_HEIGHT - MARGIN;
        }
        return currentY;
    }

    private static float writeText(PageContext pageContext, String text, float y, 
                                  PDType1Font font, float fontSize) throws IOException {
        PDPageContentStream cs = pageContext.getCurrentStream();
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(text);
        cs.endText();
        return y - (fontSize + 10);
    }

    private static float addSection(PageContext pageContext, String title, float y) throws IOException {
        return writeText(pageContext, title, y, PDType1Font.HELVETICA_BOLD, 16) - 5;
    }

    private static float addTextLine(PageContext pageContext, String text, float y) throws IOException {
        return addTextLine(pageContext, text, y, PDType1Font.HELVETICA, 12);
    }

    private static float addTextLine(PageContext pageContext, String text, float y, PDType1Font font) throws IOException {
        return addTextLine(pageContext, text, y, font, 12);
    }

    private static float addTextLine(PageContext pageContext, String text, float y, 
                                    PDType1Font font, float fontSize) throws IOException {
        PDPageContentStream cs = pageContext.getCurrentStream();
        // Handle text wrapping
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineY = y;

        for (String word : words) {
            String testLine = line.length() > 0 ? line + " " + word : word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
            
            if (textWidth > (PAGE_WIDTH - 2 * MARGIN) && line.length() > 0) {
                // Draw current line
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(MARGIN, lineY);
                cs.showText(line.toString());
                cs.endText();
                line = new StringBuilder(word);
                lineY -= LINE_HEIGHT;
            } else {
                line = new StringBuilder(testLine);
            }
        }

        // Draw remaining line
        if (line.length() > 0) {
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(MARGIN, lineY);
            cs.showText(line.toString());
            cs.endText();
        }

        return lineY - LINE_HEIGHT;
    }
}
