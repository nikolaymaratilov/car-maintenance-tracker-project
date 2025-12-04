package app.scheduling;

import app.avatarPdf.AvatarPdfClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PdfCleanupSchedulerTest {

    private AvatarPdfClient avatarPdfClient;
    private PdfCleanupScheduler scheduler;

    @BeforeEach
    void setup() {
        avatarPdfClient = mock(AvatarPdfClient.class);
        scheduler = new PdfCleanupScheduler(avatarPdfClient);
    }

    @Test
    void scheduledPdfCleanup_shouldCallClient() {
        when(avatarPdfClient.deleteOldPdfs()).thenReturn("Deleted 5 old PDFs");

        scheduler.scheduledPdfCleanup();

        verify(avatarPdfClient).deleteOldPdfs();
    }

    @Test
    void scheduledPdfCleanup_shouldHandleClientResponse() {
        String response = "Cleanup completed successfully";
        when(avatarPdfClient.deleteOldPdfs()).thenReturn(response);

        scheduler.scheduledPdfCleanup();

        verify(avatarPdfClient, times(1)).deleteOldPdfs();
    }
}

