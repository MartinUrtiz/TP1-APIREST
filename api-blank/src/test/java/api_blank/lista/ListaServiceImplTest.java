package api_blank.lista;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import api_blank.auth.AccesoDenegadoException;
import api_blank.auth.UsuarioAutenticado;
import api_blank.favorito.FavoritoRepository;
import api_blank.usuario.RolUsuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de ListaServiceImpl (Surefire: ./mvnw test).
 * Sin Spring ni base: sólo los objetos y un SecurityContext simulado.
 */
@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    private ListaServiceImpl listaService;

    private static final Long USUARIO_ID = 7L;
    private static final Long OTRO_USUARIO_ID = 99L;

    @BeforeEach
    void configurar() {
        listaService = new ListaServiceImpl(listaRepository, favoritoRepository);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crear_lista_quedaAsociadaAlUsuarioAutenticado() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.save(any(Lista.class)))
                .thenAnswer(invocation -> {
                    Lista lista = invocation.getArgument(0);
                    lista.setId(1L);
                    return lista;
                });

        ListaResponseDTO creada = listaService.crear(new ListaDTO("Regalos"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());

        assertEquals(USUARIO_ID, captor.getValue().getUsuarioId(),
                "la lista creada debe quedar asociada al usuario autenticado");
        assertEquals(USUARIO_ID, creada.getUsuarioId());
        assertEquals("Regalos", creada.getNombre());
    }

    @Test
    void crear_lista_elDueñoNoSeTomaDelBody() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.save(any(Lista.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ListaDTO dto = new ListaDTO("Regalos");
        listaService.crear(dto);

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());

        assertEquals(USUARIO_ID, captor.getValue().getUsuarioId(),
                "el dueño sale SIEMPRE del token autenticado, no del body");
        assertNotEquals(OTRO_USUARIO_ID, captor.getValue().getUsuarioId());
    }

    @Test
    void obtener_listaPropia_devuelveLaLista() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "Regalos", USUARIO_ID)));

        ListaResponseDTO lista = listaService.obtenerPorId(1L);

        assertEquals(1L, lista.getId());
        assertEquals(USUARIO_ID, lista.getUsuarioId());
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "De otro", OTRO_USUARIO_ID)));

        assertThrows(AccesoDenegadoException.class, () -> listaService.obtenerPorId(1L),
                "lista ajena debe ser 403 (AccesoDenegado), nunca 404");
    }

    @Test
    void obtener_listaInexistente_lanzaListaNoEncontrada() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ListaNoEncontradaException.class, () -> listaService.obtenerPorId(404L));
    }

    @Test
    void obtener_listaAjenaSiendoAdmin_noLanzaAccesoDenegado() {
        autenticar(USUARIO_ID, RolUsuario.ADMIN);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "De otro", OTRO_USUARIO_ID)));

        ListaResponseDTO lista = listaService.obtenerPorId(1L);

        assertEquals(OTRO_USUARIO_ID, lista.getUsuarioId());
    }

    @Test
    void eliminar_listaAjena_lanzaAccesoDenegadoSinBorrarNada() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "De otro", OTRO_USUARIO_ID)));

        assertThrows(AccesoDenegadoException.class, () -> listaService.eliminar(1L));
        verify(listaRepository, never()).deleteById(any());
    }

    @Test
    void obtenerTodas_usuarioComun_devuelveSoloLasSuyas() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findByUsuarioId(USUARIO_ID))
                .thenReturn(List.of(new Lista(1L, "Mía", USUARIO_ID)));

        List<ListaResponseDTO> listas = listaService.obtenerTodas();

        assertEquals(1, listas.size());
        assertEquals(USUARIO_ID, listas.get(0).getUsuarioId());
        verify(listaRepository, never()).findAll();
    }

    @Test
    void obtenerTodas_admin_devuelveTodasLasListas() {
        autenticar(USUARIO_ID, RolUsuario.ADMIN);
        when(listaRepository.findAll()).thenReturn(List.of(
                new Lista(1L, "De ana", OTRO_USUARIO_ID),
                new Lista(2L, "Mía", USUARIO_ID)));

        List<ListaResponseDTO> listas = listaService.obtenerTodas();

        assertEquals(2, listas.size());
        verify(listaRepository, never()).findByUsuarioId(any());
    }

    @Test
    void obtenerFavoritos_listaAjena_lanzaAccesoDenegado() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "De otro", OTRO_USUARIO_ID)));

        assertThrows(AccesoDenegadoException.class, () -> listaService.obtenerFavoritos(1L));
        verify(favoritoRepository, never()).findByListaId(any());
    }

    @Test
    void moverFavoritos_listaAjena_lanzaAccesoDenegado() {
        autenticar(USUARIO_ID, RolUsuario.USER);
        when(listaRepository.findById(1L))
                .thenReturn(Optional.of(new Lista(1L, "De otro", OTRO_USUARIO_ID)));

        assertThrows(AccesoDenegadoException.class,
                () -> listaService.moverFavoritos(1L, new MoverFavoritosDTO(2L)));
        verify(favoritoRepository, never()).findByListaId(any());
        verify(listaRepository, never()).deleteById(any());
    }

    @Test
    void sinUsuarioAutenticado_lanzaIllegalState() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, () -> listaService.obtenerTodas());
    }

    private void autenticar(Long id, RolUsuario rol) {
        var autenticacion = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(id, "user" + id + "@mail.com", rol), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
