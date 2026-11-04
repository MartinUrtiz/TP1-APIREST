package api_blank.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import api_blank.usuario.RolUsuario;
import api_blank.usuario.Usuario;
import api_blank.usuario.UsuarioRepository;
import api_blank.usuario.UsuarioResponseDTO;
import api_blank.usuario.UsuarioService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UsuarioResponseDTO registrar(String email, String password) {
        return usuarioService.registrar(email, password);
    }

    /**
     * Compara la contraseña enviada contra el hash BCrypt guardado.
     * Cualquier fallo (email inexistente o contraseña incorrecta) devuelve
     * la MISMA respuesta 401 con mensaje genérico: no se revela si el
     * email existe.
     */
    @Override
    public AuthResponseDTO login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        RolUsuario rol = usuario.getRol() != null ? usuario.getRol() : RolUsuario.USER;
        String token = jwtService.generarToken(usuario.getId(), usuario.getEmail(), rol);

        return new AuthResponseDTO(token, usuario.getId(), usuario.getEmail(), rol.name());
    }
}
