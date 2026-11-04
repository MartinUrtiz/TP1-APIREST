package api_blank;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL REAL vía Testcontainers para toda prueba que levante Spring.
 *
 * El contenedor se levanta UNA vez por JVM (singleton estático) y las
 * propiedades de datasource se inyectan en el contexto de Spring antes de
 * arrancarlo. Flyway corre las migraciones (V1..V6) contra esa base LIMPIA
 * en cada ejecución, igual que en CI: no hay PostgreSQL local de por medio.
 *
 * El lifecycle es manual (no @Testcontainers) para compartir un único
 * contenedor entre todas las clases *IT y no pagar el arranque por clase.
 */
public abstract class PostgresTestSupport {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void propsiedadesDeLaBase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
