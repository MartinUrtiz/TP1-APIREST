package api_blank.lista;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api_blank.auth.AccesoDenegadoException;
import api_blank.auth.UsuarioActual;
import api_blank.auth.UsuarioAutenticado;
import api_blank.favorito.Favorito;
import api_blank.favorito.FavoritoRepository;
import api_blank.favorito.FavoritoResponseDTO;

/**
 * Lógica de listas + AUTORIZACIÓN POR PROPIEDAD (consigna 4).
 *
 * ¿Dónde vive la regla de autorización? En el SERVICE, no en el Controller
 * ni en un filtro/ anotaciones. Razones:
 *
 * - El filtro JWT sólo resuelve IDENTIDAD (¿quién sos?), no PROPIEDAD
 *   (¿es tuyo el recurso?): el filtro no sabe qué lista pedís.
 * - Las anotaciones (@PreAuthorize) acoplan la regla a cada método del
 *   Controller/Service y complican el caso "dueño O admin" con el mismo
 *   código repteido; acá la decisión es UNA sola, testeable en unit tests
 *   sin levantar HTTP ni Spring Security.
 * - El Service es la capa que ya conoce el dominio (Lista.usuarioId) y es
 *   el único camino a los datos: los Controllers delegan siempre, así que
 *   ningún endpoint queda sin validar aunque mañana agreguen uno nuevo.
 * - La regla exige conocer el usuario autenticado: se lee del
 *   SecurityContext (UsuarioActual), que el filtro JWT llenó. El dueño al
 *   CREAR una lista también sale de ahí — nunca del body del cliente.
 *
 * Respuestas: 404 si la lista no existe; 403 (AccesoDenegadoException)
 * si existe pero no es del usuario y no es ADMIN. Nunca 404 por propiedad
 * para no confundir "no existe" con "no es tuya".
 */
@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaServiceImpl(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    /** Devuelve sólo las listas del usuario autenticado; un ADMIN ve todas. */
    @Override
    public List<ListaResponseDTO> obtenerTodas() {
        UsuarioAutenticado usuario = UsuarioActual.obtener();
        List<Lista> listas = usuario.rol() == api_blank.usuario.RolUsuario.ADMIN
                ? listaRepository.findAll()
                : listaRepository.findByUsuarioId(usuario.id());
        return listas.stream()
                .map(this::aResponseDTO)
                .toList();
    }

    @Override
    public ListaResponseDTO obtenerPorId(Long id) {
        Lista lista = cargarListaPropia(id);
        return aResponseDTO(lista);
    }

    /** El dueño es SIEMPRE el usuario autenticado: nunca un dato del body. */
    @Override
    public ListaResponseDTO crear(ListaDTO listaDTO) {
        Lista lista = new Lista();
        lista.setNombre(listaDTO.getNombre());
        lista.setUsuarioId(UsuarioActual.id());

        Lista guardada = listaRepository.save(lista);
        return aResponseDTO(guardada);
    }

    @Override
    public List<FavoritoResponseDTO> obtenerFavoritos(Long id) {
        cargarListaPropia(id);
        return favoritoRepository.findByListaId(id).stream()
                .map(this::aFavoritoResponse)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        Lista lista = cargarListaPropia(id);

        if (favoritoRepository.existsByListaId(id)) {
            throw new ListaConFavoritosException(lista.getId());
        }

        listaRepository.deleteById(id);
    }

    /**
     * Operación transaccional explícita: reasignar los favoritos de la lista
     * origen a la destino y borrar la origen son DOS escrituras que deben
     * aplicarse juntas o ninguna (atomicidad de ACID). Sin @Transactional,
     * si la segunda falla, la primera quedaría confirmada y los favoritos
     * apuntarían a una lista ya borrada (o a una que debió eliminarse).
     */
    @Transactional
    @Override
    public MoverFavoritosResponseDTO moverFavoritos(Long origenId, MoverFavoritosDTO moverDTO) {
        Long destinoId = moverDTO.getDestinoId();

        if (origenId.equals(destinoId)) {
            throw new ListasIgualesException(origenId);
        }

        // Primero se valida TODO (incluida la propiedad de AMBAS listas):
        // si algo falta o no es tuyo, no se escribe nada (404 / 403)
        Lista origen = cargarListaPropia(origenId);
        Lista destino = cargarListaPropia(destinoId);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito favorito : favoritos) {
            favorito.setListaId(destinoId);
            favoritoRepository.save(favorito);
        }

        listaRepository.deleteById(origen.getId());

        return new MoverFavoritosResponseDTO(
                origen.getId(),
                destino.getId(),
                origen.getNombre(),
                destino.getNombre(),
                favoritos.size());
    }

    /**
     * Carga una lista exigiendo que pertenezca al usuario autenticado
     * (o que el usuario sea ADMIN).
     *
     * - No existe -> {@link ListaNoEncontradaException} (404).
     * - Existe pero no es del usuario y no es ADMIN ->
     *   {@link AccesoDenegadoException} (403, nunca 404).
     */
    private Lista cargarListaPropia(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new ListaNoEncontradaException(id));

        UsuarioAutenticado usuario = UsuarioActual.obtener();
        boolean esDuenio = lista.getUsuarioId() != null && lista.getUsuarioId().equals(usuario.id());
        boolean esAdmin = usuario.rol() == api_blank.usuario.RolUsuario.ADMIN;

        if (!esDuenio && !esAdmin) {
            throw new AccesoDenegadoException("la lista " + id);
        }
        return lista;
    }

    private ListaResponseDTO aResponseDTO(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre(), lista.getUsuarioId());
    }

    private FavoritoResponseDTO aFavoritoResponse(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getProductoId(),
                favorito.getNotaPersonal(),
                favorito.getFechaCreacion(),
                favorito.getListaId());
    }
}
