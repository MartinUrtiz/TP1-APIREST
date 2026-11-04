package api_blank.usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (contrato) de persistencia de usuarios.
 * Misma forma que FavoritoRepository/ListaRepository + los buscadores por
 * email que necesitan el registro y el login.
 */
public interface UsuarioRepository {

    List<Usuario> findAll();

    Optional<Usuario> findById(Long id);

    Usuario save(Usuario usuario);

    void deleteById(Long id);

    boolean existsById(Long id);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
