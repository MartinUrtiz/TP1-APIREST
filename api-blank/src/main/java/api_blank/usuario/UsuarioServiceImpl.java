package api_blank.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * La contraseña se guarda EXCLUSIVAMENTE como hash (BCrypt, con sal
     * incorporada): ni el dominio, ni la base, ni la respuesta HTTP contienen
     * nunca la contraseña en texto plano.
     */
    @Override
    public UsuarioResponseDTO registrar(String email, String password) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(RolUsuario.USER);

        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(guardado.getId(), guardado.getEmail(), guardado.getRol().name());
    }
}
