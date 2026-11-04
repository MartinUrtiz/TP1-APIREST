package api_blank.usuario;

/**
 * Se lanza al registrarse con un email que ya está en uso -> 409 Conflict.
 */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("El email ya está registrado: " + email);
    }
}
