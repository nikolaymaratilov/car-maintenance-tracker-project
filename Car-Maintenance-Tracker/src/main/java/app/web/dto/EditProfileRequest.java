package app.web.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {

    @Size(min = 6, max = 26, message = "Username length must be between 6 and 26 symbols")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Nullable
    private String currentPassword;

    @Nullable
    private String newPassword;

    @Nullable
    @Pattern(regexp = "^$|^https?://.+", message = "Invalid URL format. Must be a valid HTTP/HTTPS URL or empty")
    private String profilePictureUrl;
}
