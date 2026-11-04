package api_blank.usuario;

/**
 * Roles de usuario. El valor se persiste como String (EnumType.STRING)
 * y viaja dentro del JWT.
 */
public enum RolUsuario {
    USER,
    ADMIN
}
