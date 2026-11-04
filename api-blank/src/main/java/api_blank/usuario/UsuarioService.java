package api_blank.usuario;

public interface UsuarioService {

    /**
     * Registra un usuario nuevo con contraseña hasheada y rol USER.
     * Lanza {@link EmailYaRegistradoException} si el email ya existe.
     */
    UsuarioResponseDTO registrar(String email, String password);
}
