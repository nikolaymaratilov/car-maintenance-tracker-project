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

    @NotBlank
    @Size(min = 6,max = 12)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6,max = 12)
    private String password;

    @NotBlank
    private String repeatPassword;
}
