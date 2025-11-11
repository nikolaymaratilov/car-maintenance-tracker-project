package app.user.service;

import app.exception.DomainException;
import app.exception.ValidationException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
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


    @Transactional
    public void createNewUser(RegisterRequest registerRequest) {

        List<String> errors = new ArrayList<>();

        if (!registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
            errors.add("Passwords do not match");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            errors.add("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            errors.add("Email is already taken");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }


        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .enabled(true)
                .profilePictureUrl("")
                .roles(java.util.Set.of(UserRole.USER))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public void updateProfile(User user, EditProfileRequest request) {

        user.setUsername(request.getUsername());

        user.setEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            throw DomainException.invalidPassword();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));


        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }
}
