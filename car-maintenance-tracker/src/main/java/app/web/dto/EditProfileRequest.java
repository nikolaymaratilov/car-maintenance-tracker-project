package app.web.dto;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
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

   @Size(min = 6,max = 12, message = "Username length must be between 6 and 26 symbols")
    private String username;

    @Email
    private String email;

    @Size(min = 6,max = 12,message = "Password length must be between 6 and 12 symbols")
    private String currentPassword;

   @Size(min = 6,max = 12,message = "Password length must be between 6 and 12 symbols")
    private String newPassword;

    @URL
    private String profilePictureUrl;

}
