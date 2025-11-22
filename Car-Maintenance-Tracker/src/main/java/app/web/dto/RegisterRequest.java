package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Required field!")
    @Size(min = 6,max = 12, message = "Username length must be between 6 and 26 symbols")
    private String username;

    @NotBlank(message = "Required field!")
    @Email
    private String email;

    @NotBlank(message = "Required field!")
    @Size(min = 6,max = 12,message = "Password length must be between 6 and 12 symbols")
    private String password;

    @NotBlank(message = "Required field!")
    private String repeatPassword;
}
