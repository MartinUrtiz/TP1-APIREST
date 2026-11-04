package api_blank.favorito;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {

    List<Favorito> findAll();

    Optional<Favorito> findById(Long id);

    Favorito save(Favorito favorito);

    void deleteById(Long id);

    boolean existsById(Long id);

    /** Favoritos que pertenecen a la lista dada (consulta derivada sobre JPA). */
    List<Favorito> findByListaId(Long listaId);

    /** Se usa para detectar listas con favoritos antes de eliminarlas (409). */
    boolean existsByListaId(Long listaId);
}
