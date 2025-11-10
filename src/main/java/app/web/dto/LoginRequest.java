package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(min = 6, message="Username must be at least 6 symbols")
    private String username;

    @NotBlank
    @Size(min = 6, message="Password must be at least 6 symbols")
    private String password;


}
