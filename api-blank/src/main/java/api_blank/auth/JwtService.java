package api_blank.auth;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import api_blank.usuario.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * Emisión y validación de JWT firmados con HMAC-SHA256.
 *
 * El token lleva: sub = id del usuario, claims email y rol.
 * El secreto y la expiración llegan por variables de entorno
 * (JWT_SECRET / JWT_EXPIRATION_MS) con valores por defecto para local.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes());
        this.expiracionMs = expiracionMs;
    }

    /** Firma un token para el usuario dado. */
    public String generarToken(Long usuarioId, String email, RolUsuario rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .claim("rol", rol.name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida la firma y la expiración, y devuelve los claims.
     * Lanza {@link JwtException} si el token es inválido o vencido.
     */
    public Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extrae la identidad del token, o null si no es válido. */
    public UsuarioAutenticado extraerUsuario(String token) {
        try {
            Claims claims = parsear(token);
            Long id = Long.valueOf(claims.getSubject());
            String email = claims.get("email", String.class);
            RolUsuario rol = RolUsuario.valueOf(claims.get("rol", String.class));
            return new UsuarioAutenticado(id, email, rol);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
