package api_blank.auth;

import api_blank.usuario.UsuarioResponseDTO;

public interface AuthService {

    /** Registra un usuario nuevo (rol USER, contraseña hasheada). */
    UsuarioResponseDTO registrar(String email, String password);

    /**
     * Valida credenciales y emite un JWT.
     * Lanza {@link CredencialesInvalidasException} (→ 401) si fallan.
     */
    AuthResponseDTO login(String email, String password);
}
