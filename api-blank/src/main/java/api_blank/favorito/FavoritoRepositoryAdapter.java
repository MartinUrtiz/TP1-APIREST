package api_blank.favorito;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import api_blank.lista.ListaJpaRepository;

/**
 * Adapter concreto del puerto {@link FavoritoRepository}, respaldado por
 * PostgreSQL a través de JPA/Hibernate.
 *
 * Reemplaza al adapter en memoria del TP1 (InMemoryFavoritoRepository /
 * FavoritoRepositoryImpl). Puertas adentro convierte
 * {@link FavoritoEntity} ↔ {@link Favorito} y delega en
 * {@link FavoritoJpaRepository}: ni el Service ni el Controller se enteran
 * de que el almacenamiento cambió.
 */
@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository favoritoJpaRepository;
    private final ListaJpaRepository listaJpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository favoritoJpaRepository,
            ListaJpaRepository listaJpaRepository) {
        this.favoritoJpaRepository = favoritoJpaRepository;
        this.listaJpaRepository = listaJpaRepository;
    }

    @Override
    public List<Favorito> findAll() {
        return favoritoJpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return favoritoJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public Favorito save(Favorito favorito) {
        FavoritoEntity guardado = favoritoJpaRepository.save(aEntidad(favorito));
        return aDominio(guardado);
    }

    @Override
    public void deleteById(Long id) {
        favoritoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return favoritoJpaRepository.existsById(id);
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return favoritoJpaRepository.findByListaId(listaId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public boolean existsByListaId(Long listaId) {
        return favoritoJpaRepository.existsByListaId(listaId);
    }

    // ------------------------------------------------------------------
    // Conversión entre la representación de infraestructura (JPA) y dominio
    // ------------------------------------------------------------------

    private FavoritoEntity aEntidad(Favorito favorito) {
        FavoritoEntity entidad = new FavoritoEntity();
        entidad.setId(favorito.getId());
        entidad.setProductoId(favorito.getProductoId());
        entidad.setNota(favorito.getNotaPersonal());
        entidad.setFechaAlta(favorito.getFechaCreacion());
        // El dominio referencia por id; JPA necesita la entidad de la lista
        if (favorito.getListaId() != null) {
            entidad.setLista(listaJpaRepository.getReferenceById(favorito.getListaId()));
        }
        return entidad;
    }

    private Favorito aDominio(FavoritoEntity entidad) {
        Favorito favorito = new Favorito();
        favorito.setId(entidad.getId());
        favorito.setProductoId(entidad.getProductoId());
        favorito.setNotaPersonal(entidad.getNota());
        favorito.setFechaCreacion(entidad.getFechaAlta());
        // @ManyToOne EAGER: la lista ya viene cargada, se lee el id sin
        // necesidad de una sesión abierta (open-in-view=false)
        if (entidad.getLista() != null) {
            favorito.setListaId(entidad.getLista().getId());
        }
        return favorito;
    }
}
