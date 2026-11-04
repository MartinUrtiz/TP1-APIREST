package api_blank.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Entidad de dominio Usuario: dueño de las listas")
public class Usuario {

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Email del usuario (único)", example = "ana@mail.com")
    private String email;

    /**
     * Hash BCrypt de la contraseña. Nunca es la contraseña en texto plano:
     * el dominio sólo la transporta; generarla/cotejarla es trabajo del
     * PasswordEncoder de Spring Security.
     */
    private String passwordHash;

    @Schema(description = "Rol del usuario", example = "USER")
    private RolUsuario rol;

    public Usuario() {
    }

    public Usuario(Long id, String email, String passwordHash, RolUsuario rol) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }
}
