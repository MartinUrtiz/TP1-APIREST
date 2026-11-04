package api_blank;

import org.junit.jupiter.api.Test;

/**
 * Verifica que la aplicación completa levanta con el contexto de seguridad
 * (JWT + filtros + autorización) contra un PostgreSQL real de
 * Testcontainers, con Flyway aplicando todas las migraciones.
 * Ejecutada por Failsafe: ./mvnw verify
 */
class ApiBlankApplicationIT extends ApiITBase {

    @Test
    void contexto_cargaConSeguridadYBaseReal() {
        // Si el contexto no levanta (falta bean, migración rota, etc.) el test falla
    }

    @Test
    void health_esPublicoYResponde200SinToken() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void migraciones_V5yV6_aplicadasEnLaBaseLimpia() {
        Long usuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Long.class);
        Long listasSinDuenio = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM listas WHERE usuario_id IS NULL", Long.class);

        org.junit.jupiter.api.Assertions.assertEquals(0L, usuarios);
        org.junit.jupiter.api.Assertions.assertEquals(0L, listasSinDuenio);
    }
}
