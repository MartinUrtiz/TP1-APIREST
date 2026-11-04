package api_blank.auth;

import api_blank.usuario.RolUsuario;

/**
 * Identidad del usuario autenticado: es el "principal" que el filtro JWT
 * deja en el SecurityContext y del cual leen los Services para autorizar.
 */
public record UsuarioAutenticado(Long id, String email, RolUsuario rol) {
}
