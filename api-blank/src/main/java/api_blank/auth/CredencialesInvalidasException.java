package api_blank.auth;

/**
 * Credenciales inválidas en login -> 401 Unauthorized.
 * El mensaje es genérico a propósito: no revela si el email existe.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
