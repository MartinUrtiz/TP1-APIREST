package api_blank.auth;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import api_blank.usuario.RolUsuario;
import api_blank.usuario.Usuario;
import api_blank.usuario.UsuarioRepository;
import api_blank.usuario.UsuarioService;
import io.jsonwebtoken.Claims;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de AuthServiceImpl (Surefire: ./mvnw test).
 * Login: coteja el hash BCrypt y emite un JWT con id y rol adentro.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String SECRET_PRUEBA =
            "clave-secreta-de-pruebas-suficientemente-larga-1234567890";
    private static final String EMAIL = "ana@mail.com";
    private static final String PASSWORD = "secreto123";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(SECRET_PRUEBA, 3600000L);
    private AuthServiceImpl authService;

    @BeforeEach
    void configurar() {
        // Se construye DESPUÉS de que Mockito inyecta los @Mock
        authService = new AuthServiceImpl(usuarioRepository, usuarioService, passwordEncoder, jwtService);
    }

    @Test
    void login_credencialesValidas_devuelveTokenFirmado() {
        dadoUsuarioConPassword();

        AuthResponseDTO respuesta = authService.login(EMAIL, PASSWORD);

        assertNotNull(respuesta.getToken(), "debe devolver un token");
        assertEquals(EMAIL, respuesta.getEmail());
        assertEquals("USER", respuesta.getRol());
        assertEquals("Bearer", respuesta.getTipo());

        Claims claims = jwtService.parsear(respuesta.getToken());
        assertEquals("42", claims.getSubject(), "el id del usuario debe estar dentro del JWT");
        assertEquals(EMAIL, claims.get("email", String.class));
        assertEquals("USER", claims.get("rol", String.class));
        assertNotNull(claims.getExpiration(), "el token debe tener expiración");
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidas() {
        dadoUsuarioConPassword();

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(EMAIL, "clave-mala"));
    }

    @Test
    void login_emailInexistente_lanzaCredencialesInvalidas() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(EMAIL, PASSWORD));
    }

    @Test
    void login_mismoErrorDeMensajeParaEmailInexistenteYPasswordMala() {
        // No se revela si el email existe: mismo mensaje en ambos casos
        String mensajePasswordMala;
        dadoUsuarioConPassword();
        try {
            authService.login(EMAIL, "clave-mala");
            throw new AssertionError("debió fallar");
        } catch (CredencialesInvalidasException e) {
            mensajePasswordMala = e.getMessage();
        }

        when(usuarioRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());
        String mensajeEmailInexistente = assertThrows(CredencialesInvalidasException.class,
                () -> authService.login("nadie@mail.com", PASSWORD)).getMessage();

        assertEquals(mensajePasswordMala, mensajeEmailInexistente);
    }

    private void dadoUsuarioConPassword() {
        Usuario usuario = new Usuario(42L, EMAIL, passwordEncoder.encode(PASSWORD), RolUsuario.USER);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    }

    @Test
    void login_tokenFirmadoConOtroSecreto_noValida() {
        dadoUsuarioConPassword();
        String token = authService.login(EMAIL, PASSWORD).getToken();

        JwtService jwtAjeno = new JwtService(
                "otro-secreto-completamente-distinto-para-la-prueba-123", 3600000L);

        org.junit.jupiter.api.Assertions.assertNull(jwtAjeno.extraerUsuario(token),
                "un token firmado con otro secreto no debe validar (da 401)");
        assertNotNull(jwtService.extraerUsuario(token), "con NUESTRO secreto sí valida");
    }

    @Test
    void login_tokenVencido_noValida() {
        JwtService jwtVencido = new JwtService(SECRET_PRUEBA, -1000L);

        String token = jwtVencido.generarToken(42L, EMAIL, RolUsuario.USER);

        org.junit.jupiter.api.Assertions.assertNull(jwtService.extraerUsuario(token),
                "un token vencido no debe validar (da 401)");
    }
}
