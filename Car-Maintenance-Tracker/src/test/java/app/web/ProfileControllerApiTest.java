package app.web;

import app.avatarPdf.AvatarPdfService;
import app.avatarPdf.dto.AvatarPdfResponse;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AvatarPdfService avatarPdfService;

    private User fakeUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        u.setPassword("pass");
        u.setEmail("john@mail.com");
        u.setEnabled(true);
        u.setRole(UserRole.USER);
        u.setCreatedOn(java.time.LocalDateTime.now());
        u.setUpdatedOn(java.time.LocalDateTime.now());
        u.setProfilePictureUrl("https://example.com/avatar.jpg");
        return u;
    }

    private UsernamePasswordAuthenticationToken auth(UUID id) {
        UserData principal = new UserData(id, UserRole.USER, "john", "pass", true);
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
    }

    @Test
    void getProfilePage_shouldReturnProfileViewAndModel() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(get("/profile").with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));
    }

    @Test
    void editProfile_valid_shouldRedirect() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/profile/edit")
                        .param("username", "john123")
                        .param("email", "john@example.com")
                        .param("profilePictureUrl", "https://example.com/new-avatar.jpg")
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService).updateProfile(eq(user), any());
    }

    @Test
    void editProfile_invalidUsername_shouldReturnProfileView() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/profile/edit")
                        .param("username", "abc")  // Too short (min 6)
                        .param("email", "john@example.com")
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void editProfile_invalidEmail_shouldReturnProfileView() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/profile/edit")
                        .param("username", "john123")
                        .param("email", "invalid-email")  // Invalid email format
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void editProfile_invalidProfilePictureUrl_shouldReturnProfileView() throws Exception {
        UUID id = UUID.randomUUID();
        User user = fakeUser(id);

        when(userService.getById(id)).thenReturn(user);

        mockMvc.perform(post("/profile/edit")
                        .param("username", "john123")
                        .param("email", "john@example.com")
                        .param("profilePictureUrl", "not-a-valid-url")  // Invalid URL format
                        .with(authentication(auth(id)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileRequest"));

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    void generateAvatarPdf_shouldReturnPdfFile() throws Exception {
        UUID id = UUID.randomUUID();
        byte[] pdfBytes = "fake pdf content".getBytes();

        when(avatarPdfService.generatePdf(any(MultipartFile.class), eq("test-name")))
                .thenReturn(pdfBytes);

        mockMvc.perform(multipart("/profile/avatar/pdf")
                        .file("file", "test content".getBytes())
                        .param("name", "test-name")
                        .with(authentication(auth(id)))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=avatar.pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(avatarPdfService).generatePdf(any(MultipartFile.class), eq("test-name"));
    }

    @Test
    void viewPdfInfo_shouldReturnAvatarPdfResponse() throws Exception {
        UUID id = UUID.randomUUID();
        UUID pdfId = UUID.randomUUID();

        AvatarPdfResponse response = new AvatarPdfResponse();
        response.setId(pdfId);
        response.setDisplayName("test-pdf");
        response.setUserId(id);
        response.setUsername("john");
        response.setEmail("john@mail.com");
        response.setRole("USER");
        response.setTotalCars(2);
        response.setTotalMaintenances(5);
        response.setTotalMaintenanceCost(BigDecimal.valueOf(500.00));

        when(avatarPdfService.getPdf(pdfId)).thenReturn(response);

        mockMvc.perform(get("/profile/pdf/" + pdfId)
                        .with(authentication(auth(id))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pdfId.toString()))
                .andExpect(jsonPath("$.displayName").value("test-pdf"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.totalCars").value(2))
                .andExpect(jsonPath("$.totalMaintenances").value(5))
                .andExpect(jsonPath("$.totalMaintenanceCost").value(500.00));

        verify(avatarPdfService).getPdf(pdfId);
    }
}

