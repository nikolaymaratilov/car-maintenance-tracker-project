package app.web.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

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

    @URL(message = "Invalid URL format")
    private String profilePictureUrl;
}
