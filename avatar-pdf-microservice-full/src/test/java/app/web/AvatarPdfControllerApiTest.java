package app.web;

import app.domain.AvatarPdf;
import app.service.AvatarPdfService;
import app.web.dto.AvatarPdfResponse;
import app.web.mapper.AvatarPdfMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvatarPdfController.class)
class AvatarPdfControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvatarPdfService service;

    @MockBean
    private AvatarPdfMapper mapper;

    @Test
    void createPdfFromUpload_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();
        AvatarPdf pdf = new AvatarPdf("test-pdf", pdfBytes);

        when(service.createFromUpload(any(), eq("TestName"))).thenReturn(pdf);

        mockMvc.perform(multipart("/api/avatar-pdfs/upload")
                        .file("file", "test content".getBytes())
                        .param("displayName", "TestName")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=avatar.pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(service).createFromUpload(any(), eq("TestName"));
    }

    @Test
    void createPdfFromUpload_withoutDisplayName_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();
        AvatarPdf pdf = new AvatarPdf(null, pdfBytes);

        when(service.createFromUpload(any(), isNull())).thenReturn(pdf);

        mockMvc.perform(multipart("/api/avatar-pdfs/upload")
                        .file("file", "test content".getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(service).createFromUpload(any(), isNull());
    }

    @Test
    void createPdfFromUpload_withException_shouldReturnBadRequest() throws Exception {
        when(service.createFromUpload(any(), anyString()))
                .thenThrow(new app.exception.PdfGenerationException("Error creating PDF"));

        mockMvc.perform(multipart("/api/avatar-pdfs/upload")
                        .file("file", "test content".getBytes())
                        .param("displayName", "TestName")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPdfFromUploadWithProfile_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();
        AvatarPdf pdf = new AvatarPdf("test-pdf", pdfBytes);
        String userProfileDataJson = "{\"totalCars\":2}";

        when(service.createFromUploadWithUserDataJson(any(), eq("TestName"), eq(userProfileDataJson)))
                .thenReturn(pdf);

        mockMvc.perform(multipart("/api/avatar-pdfs/upload-with-profile")
                        .file("file", "test content".getBytes())
                        .param("displayName", "TestName")
                        .param("userProfileData", userProfileDataJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=user-profile.pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(service).createFromUploadWithUserDataJson(any(), eq("TestName"), eq(userProfileDataJson));
    }

    @Test
    void createPdfFromUploadWithProfile_withoutDisplayName_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();
        AvatarPdf pdf = new AvatarPdf(null, pdfBytes);
        String userProfileDataJson = "{\"totalCars\":2}";

        when(service.createFromUploadWithUserDataJson(any(), isNull(), eq(userProfileDataJson)))
                .thenReturn(pdf);

        mockMvc.perform(multipart("/api/avatar-pdfs/upload-with-profile")
                        .file("file", "test content".getBytes())
                        .param("userProfileData", userProfileDataJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(service).createFromUploadWithUserDataJson(any(), isNull(), eq(userProfileDataJson));
    }

    @Test
    void createPdfFromUploadWithProfile_withException_shouldReturnBadRequest() throws Exception {
        String userProfileDataJson = "{\"totalCars\":2}";
        when(service.createFromUploadWithUserDataJson(any(), any(), eq(userProfileDataJson)))
                .thenThrow(new app.exception.PdfGenerationException("Error creating PDF"));

        mockMvc.perform(multipart("/api/avatar-pdfs/upload-with-profile")
                        .file("file", "test content".getBytes())
                        .param("displayName", "TestName")
                        .param("userProfileData", userProfileDataJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPdf_shouldReturnResponse() throws Exception {
        UUID id = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        try {
            java.lang.reflect.Field idField = AvatarPdf.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pdf, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setId(id);
        response.setDisplayName("test-pdf");

        when(service.getById(id)).thenReturn(pdf);
        when(mapper.toResponse(pdf)).thenReturn(response);

        mockMvc.perform(get("/api/avatar-pdfs/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.displayName").value("test-pdf"));

        verify(service).getById(id);
        verify(mapper).toResponse(pdf);
    }

    @Test
    void getLatestPdfForUser_shouldReturnResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("test-pdf", "pdf".getBytes());
        pdf.setUserId(userId);

        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setUserId(userId);
        response.setDisplayName("test-pdf");

        when(service.getLatestByUserId(userId)).thenReturn(pdf);
        when(mapper.toResponse(pdf)).thenReturn(response);

        mockMvc.perform(get("/api/avatar-pdfs/user/" + userId + "/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.displayName").value("test-pdf"));

        verify(service).getLatestByUserId(userId);
        verify(mapper).toResponse(pdf);
    }

    @Test
    void getAllPdfsForUser_shouldReturnList() throws Exception {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf1 = new AvatarPdf("test1", "pdf1".getBytes());
        AvatarPdf pdf2 = new AvatarPdf("test2", "pdf2".getBytes());

        AvatarPdfResponse response1 = new AvatarPdfResponse();
        response1.setDisplayName("test1");
        AvatarPdfResponse response2 = new AvatarPdfResponse();
        response2.setDisplayName("test2");

        when(service.getAllByUserId(userId)).thenReturn(List.of(pdf1, pdf2));
        when(mapper.toResponse(pdf1)).thenReturn(response1);
        when(mapper.toResponse(pdf2)).thenReturn(response2);

        mockMvc.perform(get("/api/avatar-pdfs/user/" + userId + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].displayName").value("test1"))
                .andExpect(jsonPath("$[1].displayName").value("test2"));

        verify(service).getAllByUserId(userId);
        verify(mapper, times(2)).toResponse(any());
    }

    @Test
    void deletePdf_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/avatar-pdfs/" + id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

    @Test
    void deleteLatestPdfForUser_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("test", "pdf".getBytes());
        UUID pdfId = UUID.randomUUID();
        try {
            java.lang.reflect.Field idField = AvatarPdf.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pdf, pdfId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(service.getLatestByUserId(userId)).thenReturn(pdf);

        mockMvc.perform(delete("/api/avatar-pdfs/user/" + userId + "/latest"))
                .andExpect(status().isNoContent());

        verify(service).deleteLatestByUserId(userId);
    }

    @Test
    void deleteLatestPdfForUserPost_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        AvatarPdf pdf = new AvatarPdf("test", "pdf".getBytes());
        UUID pdfId = UUID.randomUUID();
        try {
            java.lang.reflect.Field idField = AvatarPdf.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pdf, pdfId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(service.getLatestByUserId(userId)).thenReturn(pdf);

        mockMvc.perform(post("/api/avatar-pdfs/user/" + userId + "/latest/delete"))
                .andExpect(status().isNoContent());

        verify(service).deleteLatestByUserId(userId);
    }

    @Test
    void deleteOldPdfs_shouldReturnDeletedCount() throws Exception {
        when(service.deleteOldPdfs()).thenReturn(5);

        mockMvc.perform(delete("/api/avatar-pdfs/delete-old"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted old PDFs: 5"));

        verify(service).deleteOldPdfs();
    }

    @Test
    void getPdf_whenNotFound_shouldReturnInternalServerError() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new RuntimeException("AvatarPdf not found"));

        mockMvc.perform(get("/api/avatar-pdfs/" + id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getLatestPdfForUser_whenNotFound_shouldReturnNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.getLatestByUserId(userId))
                .thenThrow(new app.exception.ResourceNotFoundException("No PDF found"));

        mockMvc.perform(get("/api/avatar-pdfs/user/" + userId + "/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePdf_whenNotFound_shouldReturnInternalServerError() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("AvatarPdf not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/avatar-pdfs/" + id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllPdfsForUser_whenEmpty_shouldReturnEmptyList() throws Exception {
        UUID userId = UUID.randomUUID();

        when(service.getAllByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/avatar-pdfs/user/" + userId + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(service).getAllByUserId(userId);
    }

    @Test
    void deleteOldPdfs_whenZeroDeleted_shouldReturnZero() throws Exception {
        when(service.deleteOldPdfs()).thenReturn(0);

        mockMvc.perform(delete("/api/avatar-pdfs/delete-old"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted old PDFs: 0"));

        verify(service).deleteOldPdfs();
    }
}
