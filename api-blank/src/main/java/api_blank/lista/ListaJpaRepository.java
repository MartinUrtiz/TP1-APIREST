package api_blank.lista;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de listas: Spring Data lo implementa solo.
 */
public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {
}
