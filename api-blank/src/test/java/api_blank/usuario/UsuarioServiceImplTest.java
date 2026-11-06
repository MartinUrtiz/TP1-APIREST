package api_blank.usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de UsuarioServiceImpl (Surefire: ./mvnw test).
 * La contraseña NUNCA debe guardarse ni devolverse en texto plano.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    /** BCrypt real (no mockeado): es el codificador que usa la app. */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UsuarioServiceImpl usuarioService;

    private static final String EMAIL = "ana@mail.com";
    private static final String PASSWORD = "secreto123";

    @BeforeEach
    void configurar() {
        // Se construye DESPUÉS de que Mockito inyecta los @Mock
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void registrar_nuncaGuardaLaPasswordEnTextoPlano() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        usuarioService.registrar(EMAIL, PASSWORD);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        String hashGuardado = captor.getValue().getPasswordHash();

        assertNotNull(hashGuardado, "se debe guardar un hash, no null");
        assertFalse(hashGuardado.isEmpty(), "el hash no puede estar vacío");
        assertFalse(hashGuardado.equals(PASSWORD), "NUNCA la contraseña en texto plano");
        assertFalse(hashGuardado.contains(PASSWORD), "el hash no contiene la contraseña");
        assertTrue(passwordEncoder.matches(PASSWORD, hashGuardado),
                "el hash guardado debe poder cotejarse con la contraseña original");
    }

    @Test
    void registrar_rolPorDefectoEsUser() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.registrar(EMAIL, PASSWORD);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        assertEquals(RolUsuario.USER, captor.getValue().getRol());
    }

    @Test
    void registrar_respuestaNoIncluyeElHash() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponseDTO respuesta = usuarioService.registrar(EMAIL, PASSWORD);

        assertEquals(EMAIL, respuesta.getEmail());
        assertEquals("USER", respuesta.getRol());
        // El DTO de salida no tiene campo password: no se filtra ni el hash ni la clave
    }

    @Test
    void registrar_emailYaExistente_lanzaEmailYaRegistradoYNoGuarda() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class,
                () -> usuarioService.registrar(EMAIL, PASSWORD));

        verify(usuarioRepository, never()).save(any());
    }
}
