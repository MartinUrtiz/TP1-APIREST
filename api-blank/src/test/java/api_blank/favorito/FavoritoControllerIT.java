package api_blank.favorito;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import api_blank.ApiITBase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración de /api/favoritos: exige token y que la lista sea del
 * usuario autenticado. Contra PostgreSQL real (Testcontainers + Flyway).
 */
class FavoritoControllerIT extends ApiITBase {

    private static final String ANA = "ana@mail.com";
    private static final String BRUNO = "bruno@mail.com";
    private static final String CLAVE = "secreto123";

    @Test
    void crearFavorito_enListaPropia_devuelve201() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String token = login(ANA, CLAVE);
        Long listaId = crearLista(token, "Regalos");

        mockMvc.perform(conToken(post("/api/favoritos"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"listaId\":" + listaId
                                + ",\"notaPersonal\":\"en oferta\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listaId").value(listaId));
    }

    @Test
    void crearFavorito_enListaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "Regalos");

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        mockMvc.perform(conToken(post("/api/favoritos"), tokenBruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"listaId\":" + listaDeAna + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearFavorito_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"listaId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarFavoritos_devuelveSoloLosDeLasListasPropias() throws Exception {
        registrarUsuario(ANA, CLAVE);
        registrarUsuario(BRUNO, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "De ana");
        Long listaDeBruno = crearLista(tokenBruno, "De bruno");

        mockMvc.perform(conToken(post("/api/favoritos"), tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"listaId\":" + listaDeAna + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(conToken(post("/api/favoritos"), tokenBruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":2,\"listaId\":" + listaDeBruno + "}"))
                .andExpect(status().isCreated());

        // Ana sólo ve SU favorito, no el de Bruno
        mockMvc.perform(conToken(get("/api/favoritos"), tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].listaId").value(listaDeAna));
    }

    @Test
    void eliminarFavoritoDeListaAjena_devuelve403() throws Exception {
        registrarUsuario(ANA, CLAVE);
        String tokenAna = login(ANA, CLAVE);
        Long listaDeAna = crearLista(tokenAna, "De ana");
        String cuerpo = mockMvc.perform(conToken(post("/api/favoritos"), tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"listaId\":" + listaDeAna + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long favoritoId = objectMapper.readTree(cuerpo).get("id").asLong();

        registrarUsuario(BRUNO, CLAVE);
        String tokenBruno = login(BRUNO, CLAVE);

        mockMvc.perform(conToken(delete("/api/favoritos/{id}", favoritoId), tokenBruno))
                .andExpect(status().isForbidden());
    }
}
