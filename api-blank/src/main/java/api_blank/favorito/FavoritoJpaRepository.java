package api_blank.favorito;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interfaz que Spring Data implementa en tiempo de ejecución (proxy) a partir
 * de las consultas derivadas y de las anotaciones que declaremos.
 *
 * Es el "adaptador fino" hacia JPA: no contiene lógica de negocio ni
 * conversión a dominio; eso vive en {@link FavoritoRepositoryAdapter}.
 */
public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {

    /** Consulta derivada: findBy + nombre del atributo (o de su camino). */
    List<FavoritoEntity> findByListaId(Long listaId);

    boolean existsByListaId(Long listaId);
}
