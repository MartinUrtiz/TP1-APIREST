package api_blank.lista;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Adapter del puerto {@link ListaRepository} respaldado por JPA/Hibernate.
 * Misma receta que FavoritoRepositoryAdapter: convertir y delegar.
 */
@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository listaJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository listaJpaRepository) {
        this.listaJpaRepository = listaJpaRepository;
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

    private ListaEntity aEntidad(Lista lista) {
        ListaEntity entidad = new ListaEntity();
        entidad.setId(lista.getId());
        entidad.setNombre(lista.getNombre());
        return entidad;
    }

    private Lista aDominio(ListaEntity entidad) {
        return new Lista(entidad.getId(), entidad.getNombre());
    }
}
