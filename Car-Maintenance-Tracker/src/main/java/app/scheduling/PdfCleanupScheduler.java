package app.scheduling;

import app.avatarPdf.AvatarPdfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PdfCleanupScheduler {

    private final AvatarPdfClient avatarPdfClient;

    public PdfCleanupScheduler(AvatarPdfClient avatarPdfClient) {
        this.avatarPdfClient = avatarPdfClient;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledPdfCleanup() {
        String result = avatarPdfClient.deleteOldPdfs();
        log.info("Scheduled PDF cleanup executed: {}", result);
    }
}
