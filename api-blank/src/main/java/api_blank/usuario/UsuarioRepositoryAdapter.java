package api_blank.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Adapter del puerto {@link UsuarioRepository} respaldado por JPA/Hibernate.
 * Misma receta que FavoritoRepositoryAdapter y ListaRepositoryAdapter.
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioJpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity guardado = usuarioJpaRepository.save(aEntidad(usuario));
        return aDominio(guardado);
    }

    @Override
    public void deleteById(Long id) {
        usuarioJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return usuarioJpaRepository.existsById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioJpaRepository.findByEmail(email).map(this::aDominio);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioJpaRepository.existsByEmail(email);
    }

    private UsuarioEntity aEntidad(Usuario usuario) {
        UsuarioEntity entidad = new UsuarioEntity();
        entidad.setId(usuario.getId());
        entidad.setEmail(usuario.getEmail());
        entidad.setPasswordHash(usuario.getPasswordHash());
        entidad.setRol(usuario.getRol());
        return entidad;
    }

    private Usuario aDominio(UsuarioEntity entidad) {
        return new Usuario(entidad.getId(), entidad.getEmail(), entidad.getPasswordHash(), entidad.getRol());
    }
}
