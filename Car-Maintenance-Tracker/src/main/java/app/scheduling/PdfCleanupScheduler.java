package app.scheduling;

import app.avatarPdf.AvatarPdfClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PdfCleanupScheduler {

    private final AvatarPdfClient avatarPdfClient;

    public PdfCleanupScheduler(AvatarPdfClient avatarPdfClient) {
        this.avatarPdfClient = avatarPdfClient;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledPdfCleanup() {
        String result = avatarPdfClient.deleteOldPdfs();
        System.out.println("Scheduled PDF cleanup executed: " + result);
    }
}
