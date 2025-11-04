package app.user.service;

import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User with [%s] id does not exist. ".formatted(id)));
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void createNewUser(RegisterRequest registerRequest) {

        if (!registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .enabled(true)
                .profilePicture("")
                .roles(java.util.Set.of(UserRole.USER))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public User login(LoginRequest loginRequest) {

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(()
                -> DomainException.userNotFound(loginRequest.getUsername()));

        String rawPassword = loginRequest.getPassword();
        String encodedPassword = user.getPassword();

        if (!passwordEncoder.matches(rawPassword,encodedPassword)){

            throw DomainException.invalidData();
        }

        return user;
    }
}