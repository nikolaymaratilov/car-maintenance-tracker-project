package app.web;

import app.avatarPdf.AvatarPdfService;
import app.avatarPdf.dto.AvatarPdfResponse;
import app.avatarPdf.dto.UserProfileData;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfController.class)
class PdfControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvatarPdfService avatarPdfService;

    @MockitoBean
    private UserService userService;

    private UsernamePasswordAuthenticationToken auth(UUID id) {
        UserData principal = new UserData(id, UserRole.USER, "john", "pass", true);
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    @Test
    void generatePdfPage_shouldReturnPdfGenerateView() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/pdf/generate")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("pdf-generate"));
    }

    @Test
    void generateFullProfilePdf_withName_shouldReturnPdf() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("john");

        UserProfileData profileData = new UserProfileData(null, null, null, 0, 0, BigDecimal.ZERO, LocalDateTime.now());
        byte[] pdfBytes = "fake pdf content".getBytes();

        when(userService.getById(id)).thenReturn(user);
        when(avatarPdfService.buildUserProfileData(id)).thenReturn(profileData);
        when(avatarPdfService.generatePdfWithProfile(any(MultipartFile.class), eq("CustomName"), eq(profileData)))
                .thenReturn(pdfBytes);

        mockMvc.perform(multipart("/pdf/generate")
                        .file("file", "test content".getBytes())
                        .param("name", "CustomName")
                        .with(authentication(auth(id)))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=user-profile.pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(avatarPdfService).buildUserProfileData(id);
        verify(avatarPdfService).generatePdfWithProfile(any(MultipartFile.class), eq("CustomName"), eq(profileData));
    }

    @Test
    void generateFullProfilePdf_withoutName_shouldUseUsername() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("john");

        UserProfileData profileData = new UserProfileData(null, null, null, 0, 0, BigDecimal.ZERO, LocalDateTime.now());
        byte[] pdfBytes = "fake pdf content".getBytes();

        when(userService.getById(id)).thenReturn(user);
        when(avatarPdfService.buildUserProfileData(id)).thenReturn(profileData);
        when(avatarPdfService.generatePdfWithProfile(any(MultipartFile.class), eq("john"), eq(profileData)))
                .thenReturn(pdfBytes);

        mockMvc.perform(multipart("/pdf/generate")
                        .file("file", "test content".getBytes())
                        .with(authentication(auth(id)))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=user-profile.pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(userService).getById(id);
        verify(avatarPdfService).buildUserProfileData(id);
        verify(avatarPdfService).generatePdfWithProfile(any(MultipartFile.class), eq("john"), eq(profileData));
    }

    @Test
    void viewLatestPdfInfo_shouldReturnPdfInfoView() throws Exception {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("john");

        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setId(UUID.randomUUID());
        response.setUserId(id);
        response.setDisplayName("latest-pdf");

        when(userService.getById(id)).thenReturn(user);
        when(avatarPdfService.getLatestPdfForUser(id)).thenReturn(response);

        mockMvc.perform(get("/pdf/latest")
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("pdf-info"))
                .andExpect(model().attributeExists("pdfInfo"))
                .andExpect(model().attributeExists("user"));

        verify(avatarPdfService).getLatestPdfForUser(id);
        verify(userService).getById(id);
    }

    @Test
    void deleteLatestPdf_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/pdf/delete-latest")
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pdf/generate?deleteSuccess=true"));

        verify(avatarPdfService).deleteLatestPdfForUser(id);
    }
}

