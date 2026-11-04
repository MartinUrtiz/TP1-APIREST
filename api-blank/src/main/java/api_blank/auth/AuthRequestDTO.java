package api_blank.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para registrarse o iniciar sesión")
public class AuthRequestDTO {

    @Schema(description = "Email del usuario", example = "ana@mail.com")
    @NotBlank(message = "el email es obligatorio")
    @Email(message = "el email no tiene un formato válido")
    private String email;

    @Schema(description = "Contraseña (mínimo 6 caracteres)", example = "secreto123")
    @NotBlank(message = "la contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "la contraseña debe tener entre 6 y 72 caracteres")
    private String password;

    public AuthRequestDTO() {
    }

    public AuthRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
