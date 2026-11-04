package api_blank.lista;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (contrato) de persistencia de listas.
 *
 * Tiene exactamente la misma forma que {@link api_blank.favorito.FavoritoRepository}:
 * la capa de dominio/servicio define QUÉ necesita y queda indiferente de CÓMO
 * se guarda (en memoria, PostgreSQL, etc.).
 */
public interface ListaRepository {

    List<Lista> findAll();

    Optional<Lista> findById(Long id);

    Lista save(Lista lista);

    void deleteById(Long id);

    boolean existsById(Long id);
}
