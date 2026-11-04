package api_blank.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta del login/registro con el JWT emitido")
public class AuthResponseDTO {

    @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Tipo de token para el header Authorization", example = "Bearer")
    private String tipo = "Bearer";

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Email del usuario", example = "ana@mail.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "USER")
    private String rol;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, Long id, String email, String rol) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
