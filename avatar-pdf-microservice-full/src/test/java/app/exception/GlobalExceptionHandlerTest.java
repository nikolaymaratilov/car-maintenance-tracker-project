package app.exception;

import app.web.AvatarPdfController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AvatarPdfController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private app.service.AvatarPdfService avatarPdfService;

    @MockBean
    private app.web.mapper.AvatarPdfMapper mapper;

    @Test
    void handlePdfGenerationException_shouldReturnBadRequest() throws Exception {
        when(avatarPdfService.createFromUpload(any(), any()))
                .thenThrow(new PdfGenerationException("PDF generation failed"));

        mockMvc.perform(multipart("/api/avatar-pdfs/upload")
                        .file("file", "test".getBytes())
                        .param("displayName", "Test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("PDF generation failed"));
    }

    @Test
    void handleResourceNotFoundException_shouldReturnNotFound() throws Exception {
        when(avatarPdfService.getLatestByUserId(any()))
                .thenThrow(new ResourceNotFoundException("PDF not found"));

        mockMvc.perform(get("/api/avatar-pdfs/user/" + java.util.UUID.randomUUID() + "/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() throws Exception {
        when(avatarPdfService.getById(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/avatar-pdfs/" + java.util.UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Internal server error")));
    }

    @Test
    void handlePdfGenerationException_withCause_shouldReturnBadRequest() throws Exception {
        when(avatarPdfService.createFromUpload(any(), any()))
                .thenThrow(new PdfGenerationException("PDF generation failed", new RuntimeException("Cause")));

        mockMvc.perform(multipart("/api/avatar-pdfs/upload")
                        .file("file", "test".getBytes())
                        .param("displayName", "Test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("PDF generation failed"));
    }

    @Test
    void handleResourceNotFoundException_withMessage_shouldReturnNotFound() throws Exception {
        when(avatarPdfService.getLatestByUserId(any()))
                .thenThrow(new ResourceNotFoundException("PDF not found for user: " + java.util.UUID.randomUUID()));

        mockMvc.perform(get("/api/avatar-pdfs/user/" + java.util.UUID.randomUUID() + "/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleGenericException_withNullMessage_shouldReturnInternalServerError() throws Exception {
        when(avatarPdfService.getById(any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/avatar-pdfs/" + java.util.UUID.randomUUID()))
                .andExpect(status().isInternalServerError());
    }
}

