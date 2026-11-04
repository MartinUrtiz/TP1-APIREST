package api_blank;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// Spring Boot 4 autoconfigura Jackson 3 (tools.jackson): el ObjectMapper
// inyectado por Spring y el JsonNode son de ese paquete.
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de las pruebas de integración (*IT, ejecutadas por Failsafe con
 * ./mvnw verify): levanta la aplicación completa contra un PostgreSQL real
 * de Testcontainers, con Flyway aplicando las migraciones sobre esa base
 * limpia. Helpers para registrar usuarios, loguearse y crear listas.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ApiITBase extends PostgresTestSupport {

    protected static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /** Base limpia en cada test (datos independientes entre tests). */
    @BeforeEach
    void limpiarBaseDePruebas() {
        jdbcTemplate.update("DELETE FROM favoritos");
        jdbcTemplate.update("DELETE FROM listas");
        jdbcTemplate.update("DELETE FROM usuarios");
        SecurityContextHolder.clearContext();
    }

    /** Registra un usuario (rol USER) y falla el test si no devuelve 201. */
    protected void registrarUsuario(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(JSON)
                        .content(credencialesJson(email, password)))
                .andExpect(status().isCreated());
    }

    /** Devuelve el JWT del login; falla el test si no devuelve 200. */
    protected String login(String email, String password) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content(credencialesJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return tokenDe(resultado);
    }

    /**
     * Registra, promueve a ADMIN en la base y devuelve un token ya emitido
     * con el rol ADMIN adentro (el JWT es el que viaja en cada request).
     */
    protected String registrarAdminYLoguear(String email, String password) throws Exception {
        registrarUsuario(email, password);
        jdbcTemplate.update("UPDATE usuarios SET rol = 'ADMIN' WHERE email = ?", email);
        return login(email, password);
    }

    /** Crea una lista con el token dado y devuelve su id. */
    protected Long crearLista(String token, String nombre) throws Exception {
        MvcResult resultado = mockMvc.perform(conToken(post("/api/listas"), token)
                        .contentType(JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    /** Agrega el header Authorization: Bearer &lt;token&gt; a un request. */
    protected MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder builder, String token) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected String tokenDe(MvcResult resultado) throws Exception {
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return cuerpo.get("token").asText();
    }

    protected String cuerpoDe(MvcResult resultado) throws Exception {
        return resultado.getResponse().getContentAsString();
    }

    protected List<String> emailsEnLaBase() {
        return jdbcTemplate.queryForList("SELECT email FROM usuarios ORDER BY id", String.class);
    }

    private String credencialesJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }
}
