package api_blank.lista;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import api_blank.usuario.UsuarioEntity;
import api_blank.usuario.UsuarioJpaRepository;

/**
 * Adapter del puerto {@link ListaRepository} respaldado por JPA/Hibernate.
 * Misma receta que FavoritoRepositoryAdapter: convertir y delegar.
 *
 * Al guardar, el dueño (usuario_id) se resuelve como referencia JPA al
 * usuario existente: el id del dueño NUNCA llega del cliente, sino del
 * usuario autenticado (ver ListaServiceImpl).
 */
@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository listaJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository listaJpaRepository,
                                  UsuarioJpaRepository usuarioJpaRepository) {
        this.listaJpaRepository = listaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public List<Lista> findAll() {
        return listaJpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return listaJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity guardada = listaJpaRepository.save(aEntidad(lista));
        return aDominio(guardada);
    }

    @Override
    public void deleteById(Long id) {
        listaJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return listaJpaRepository.existsById(id);
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return listaJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::aDominio)
                .toList();
    }

    private ListaEntity aEntidad(Lista lista) {
        ListaEntity entidad = new ListaEntity();
        entidad.setId(lista.getId());
        entidad.setNombre(lista.getNombre());
        if (lista.getUsuarioId() != null) {
            UsuarioEntity duenio = usuarioJpaRepository.findById(lista.getUsuarioId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Usuario dueño inexistente: " + lista.getUsuarioId()));
            entidad.setUsuario(duenio);
        }
        return entidad;
    }

    private Lista aDominio(ListaEntity entidad) {
        Long usuarioId = entidad.getUsuario() != null ? entidad.getUsuario().getId() : null;
        return new Lista(entidad.getId(), entidad.getNombre(), usuarioId);
    }
}
