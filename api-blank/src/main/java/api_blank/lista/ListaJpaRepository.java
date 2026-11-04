package api_blank.lista;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de listas: Spring Data lo implementa solo.
 */
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {

    /** Listas de un usuario concreto (GET /api/listas filtra por dueño). */
    List<ListaEntity> findByUsuarioId(Long usuarioId);
}
