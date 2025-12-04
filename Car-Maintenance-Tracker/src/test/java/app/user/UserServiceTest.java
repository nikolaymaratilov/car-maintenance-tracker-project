package app.user;

import app.exception.ProfileUpdateException;
import app.exception.ValidationException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void getById_shouldReturnUser() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void getById_shouldThrow_whenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getById(id));
    }

    @Test
    void createNewUser_shouldCreateSuccessfully() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("12345");
        req.setRepeatPassword("12345");
        req.setEmail("john@test.com");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("12345")).thenReturn("ENCODED");

        userService.createNewUser(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("john", saved.getUsername());
        assertEquals("ENCODED", saved.getPassword());
        assertEquals(UserRole.USER, saved.getRole());
        assertTrue(saved.isEnabled());
    }

    @Test
    void createNewUser_shouldThrow_whenPasswordsMismatch() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("a");
        req.setEmail("a@test.com");
        req.setPassword("1");
        req.setRepeatPassword("2");

        assertThrows(ValidationException.class, () -> userService.createNewUser(req));
    }

    @Test
    void createNewUser_shouldThrow_whenUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("a");
        req.setEmail("a@test.com");
        req.setPassword("1");
        req.setRepeatPassword("1");

        when(userRepository.existsByUsername("a")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.createNewUser(req));
    }

    @Test
    void createNewUser_shouldThrow_whenEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("a");
        req.setEmail("a@test.com");
        req.setPassword("1");
        req.setRepeatPassword("1");

        when(userRepository.existsByEmail("a@test.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.createNewUser(req));
    }

    @Test
    void updateProfile_shouldUpdateSuccessfully() {
        User user = User.builder()
                .username("old")
                .email("old@mail.com")
                .password("OLDPASS")
                .build();

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("new");
        req.setEmail("new@mail.com");
        req.setProfilePictureUrl("  pic.jpg ");
        req.setCurrentPassword("123");
        req.setNewPassword("newpass");

        when(passwordEncoder.matches("123", "OLDPASS")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("ENC_NEW");

        userService.updateProfile(user, req);

        assertEquals("new", user.getUsername());
        assertEquals("new@mail.com", user.getEmail());
        assertEquals("pic.jpg", user.getProfilePictureUrl());
        assertEquals("ENC_NEW", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldThrow_ifCurrentPasswordWrong() {
        User user = User.builder().password("OLDPASS").build();

        EditProfileRequest req = new EditProfileRequest();
        req.setUsername("x");
        req.setEmail("x@test.com");
        req.setNewPassword("new");
        req.setCurrentPassword("wrong");

        when(passwordEncoder.matches("wrong", "OLDPASS")).thenReturn(false);

        assertThrows(ProfileUpdateException.class, () -> userService.updateProfile(user, req));
    }

    @Test
    void getAllUsers_shouldReturnCounts() {
        List<User> users = List.of(
                User.builder().role(UserRole.USER).build(),
                User.builder().role(UserRole.ADMIN).build(),
                User.builder().role(UserRole.USER).build()
        );

        when(userRepository.findAll()).thenReturn(users);

        ArrayList<Integer> result = userService.getAllUsers();
        assertEquals(2, result.get(0));
        assertEquals(1, result.get(1));
    }

    @Test
    void switchRole_shouldToggleRole() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).role(UserRole.USER).build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.switchRole(id);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void switchStatus_shouldToggleEnabled() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).enabled(true).build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.switchStatus(id);

        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void resolveLoginMessage_shouldReturnDisabledMessage() {
        assertEquals(
                "This account has been disabled and access is temporarily unavailable.",
                userService.resolveLoginMessage(null, null, true)
        );
    }

    @Test
    void resolveLoginMessage_shouldReturnErrorMessage() {
        assertEquals(
                "Invalid username or password",
                userService.resolveLoginMessage(null, "err", false)
        );
    }

    @Test
    void resolveLoginMessage_shouldReturnAttemptMessage() {
        assertEquals(
                "attempt",
                userService.resolveLoginMessage("attempt", null, false)
        );
    }

    @Test
    void refreshCache_shouldNotThrow() {
        assertDoesNotThrow(() -> userService.refreshCache());
    }
}
