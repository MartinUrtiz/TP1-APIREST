package api_blank.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Lectura de la identidad autenticada desde el SecurityContext.
 *
 * El filtro JWT deja el UsuarioAutenticado ahí una vez validado el token;
 * los Services lo usan para autorizar (dueño del recurso / rol ADMIN).
 */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static UsuarioAutenticado obtener() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad");
        }
        return usuario;
    }

    public static Long id() {
        return obtener().id();
    }

    public static boolean esAdmin() {
        return obtener().rol() == api_blank.usuario.RolUsuario.ADMIN;
    }
}
