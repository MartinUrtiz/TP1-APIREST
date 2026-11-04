package api_blank.lista;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import api_blank.ApiITBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración de /api/listas: propiedad de recursos y rol, contra
 * PostgreSQL real (Testcontainers + Flyway). Ejecutadas por Failsafe.
 */
class ListaControllerIT extends ApiITBase {

    private static final String ANA = "ana@mail.com";
    private static final String BRUNO = "bruno@mail.com";
    private static final String CLAVE = "secreto123";

    @Test
    void crear_lista_quedaAsociadaAlUsuarioAutenticado() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String token = login(ANA, CLAVE);
        Long anaId = idDeUsuario(ANA);

        mockMvc.perform(conToken(post("/api/listas"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Regalos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Regalos"))
                // El dueño sale del token, nunca del body del cliente
                .andExpect(jsonPath("$.usuarioId").value(anaId));
    }

    @Test
    void obtener_listaPropia_devuelve200() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String token = login(ANA, CLAVE);
        Long listaId = crearLista(token, "Regalos");

        mockMvc.perform(conToken(get("/api/listas/{id}", listaId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaId))
                .andExpect(jsonPath("$.nombre").value("Regalos"));
    }

    @Test
    void obtener_listaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Regalos");

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        // Existe, pero no es de Bruno y no es ADMIN: 403, nunca 404
        mockMvc.perform(conToken(get("/api/listas/{id}", listaDeAna), tokenBruno))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtener_listasSinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaPropiaSinToken_devuelve401() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String token = login(ANA, CLAVE);
        Long listaId = crearLista(token, "Regalos");

        mockMvc.perform(get("/api/listas/{id}", listaId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_tokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas").header("Authorization", "Bearer token-falsito"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtener_listaAjenaSiendoAdmin_devuelve200() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Regalos");

        String tokenAdmin = registrarAdminYLoguear(BRUNO, CLAVE);

        mockMvc.perform(conToken(get("/api/listas/{id}", listaDeAna), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listaDeAna));
    }

    @Test
    void obtener_listas_devuelveSoloLasDelUsuarioAutenticado() throws Exception {
        registrarUsuario(ANA, CLAVE);
        registrarUsuario(BRUNO, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);
        crearLista(tokenAna, "De ana 1");
        crearLista(tokenAna, "De ana 2");
        crearLista(tokenBruno, "De bruno");

        mockMvc.perform(conToken(get("/api/listas"), tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(conToken(get("/api/listas"), tokenBruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtener_listasSiendoAdmin_devuelveTodas() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        crearLista(tokenAna, "De ana 1");
        crearLista(tokenAna, "De ana 2");

        String tokenAdmin = registrarAdminYLoguear(BRUNO, CLAVE);

        mockMvc.perform(conToken(get("/api/listas"), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void eliminar_listaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Regalos");

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        mockMvc.perform(conToken(delete("/api/listas/{id}", listaDeAna), tokenBruno))
                .andExpect(status().isForbidden());

        // La lista sigue ahí: no se borró nada
        mockMvc.perform(conToken(get("/api/listas/{id}", listaDeAna), tokenAna))
                .andExpect(status().isOk());
    }

    @Test
    void moverFavoritos_listaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Origen");
        Long destinoDeAna = crearLista(tokenAna, "Destino");

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        mockMvc.perform(conToken(post("/api/listas/{origenId}/mover-favoritos", listaDeAna), tokenBruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinoId\":" + destinoDeAna + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtener_favoritosDeListaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Regalos");

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        mockMvc.perform(conToken(get("/api/listas/{id}/favoritos", listaDeAna), tokenBruno))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtener_listaInexistente_devuelve404() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String token = login(ANA, CLAVE);

        mockMvc.perform(conToken(get("/api/listas/{id}", 999999L), token))
                .andExpect(status().isNotFound());
    }

    private Long idDeUsuario(String email) {
        return jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, email);
    }
}
