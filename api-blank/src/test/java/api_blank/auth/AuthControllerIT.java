package api_blank.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import api_blank.ApiITBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración de /api/auth contra PostgreSQL real (Testcontainers + Flyway).
 * Ejecutadas por Failsafe: ./mvnw verify
 */
class AuthControllerIT extends ApiITBase {

    private static final String CREDENCIALES = "{\"email\":\"ana@mail.com\",\"password\":\"secreto123\"}";

    @Test
    void registro_credencialesValidas_devuelve201() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREDENCIALES))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void registro_emailYaRegistrado_devuelve409() throws Exception {
        registrarUsuario("ana@mail.com", "secreto123");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREDENCIALES))
                .andExpect(status().isConflict());
    }

    @Test
    void registro_passwordNoSeGuardaEnTextoPlano() throws Exception {
        registrarUsuario("ana@mail.com", "secreto123");

        String hashGuardado = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM usuarios WHERE email = ?", String.class, "ana@mail.com");

        org.junit.jupiter.api.Assertions.assertNotNull(hashGuardado);
        org.junit.jupiter.api.Assertions.assertNotEquals("secreto123", hashGuardado);
        org.junit.jupiter.api.Assertions.assertTrue(hashGuardado.startsWith("$2"),
                "el hash debe ser BCrypt, empieza con $2: " + hashGuardado);
    }

    @Test
    void login_credencialesValidas_devuelve200ConToken() throws Exception {
        registrarUsuario("ana@mail.com", "secreto123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREDENCIALES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void login_passwordIncorrecta_devuelve401() throws Exception {
        registrarUsuario("ana@mail.com", "secreto123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@mail.com\",\"password\":\"otra-clave\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_emailInexistente_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREDENCIALES))
                .andExpect(status().isUnauthorized());
    }
}
