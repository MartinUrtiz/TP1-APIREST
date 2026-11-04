package api_blank.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de salida para un usuario (sin la contraseña)")
public class UsuarioResponseDTO {

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Email del usuario", example = "ana@mail.com")
    private String email;

    @Schema(description = "Rol del usuario", example = "USER")
    private String rol;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String email, String rol) {
        this.id = id;
        this.email = email;
        this.rol = rol;
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
