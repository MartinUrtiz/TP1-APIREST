package api_blank.favorito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import api_blank.auth.AccesoDenegadoException;
import api_blank.auth.UsuarioActual;
import api_blank.auth.UsuarioAutenticado;
import api_blank.lista.ListaNoEncontradaException;
import api_blank.lista.ListaRepository;
import api_blank.usuario.RolUsuario;

/**
 * Lógica de favoritos + AUTORIZACIÓN POR PROPIEDAD.
 *
 * Un favorito cuelga de una lista y la lista pertenece a un usuario
 * (Usuario → Lista → Favorito). La regla vive en el SERVICE (ver el
 * javadoc de {@link api_blank.lista.ListaServiceImpl} por qué): toda
 * operación sobre un favorito valida primero que SU lista pertenezca al
 * usuario autenticado (o que sea ADMIN). Sin token, el filtro devuelve
 * 401 antes de llegar acá; con token ajeno, 403 (AccesoDenegadoException).
 */
@Service
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ListaRepository listaRepository;

    public FavoritoServiceImpl(FavoritoRepository favoritoRepository, ListaRepository listaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.listaRepository = listaRepository;
    }

    /** Sólo los favoritos de las listas del usuario autenticado (ADMIN: todos). */
    @Override
    public List<FavoritoResponseDTO> obtenerTodos() {
        UsuarioAutenticado usuario = UsuarioActual.obtener();

        if (usuario.rol() == RolUsuario.ADMIN) {
            return favoritoRepository.findAll().stream()
                    .map(this::aResponseDTO)
                    .toList();
        }

        Set<Long> misListas = listaRepository.findByUsuarioId(usuario.id()).stream()
                .map(lista -> lista.getId())
                .collect(Collectors.toSet());

        return favoritoRepository.findAll().stream()
                .filter(favorito -> misListas.contains(favorito.getListaId()))
                .map(this::aResponseDTO)
                .toList();
    }

    @Override
    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));
        cargarListaPropia(favorito.getListaId());
        return aResponseDTO(favorito);
    }

    @Override
    public FavoritoResponseDTO crear(FavoritoDTO favoritoDTO) {
        cargarListaPropia(favoritoDTO.getListaId());

        Favorito favorito = new Favorito();
        favorito.setProductoId(favoritoDTO.getProductoId());
        favorito.setNotaPersonal(favoritoDTO.getNotaPersonal());
        favorito.setFechaCreacion(LocalDateTime.now());
        favorito.setListaId(favoritoDTO.getListaId());

        Favorito guardado = favoritoRepository.save(favorito);
        return aResponseDTO(guardado);
    }

    @Override
    public FavoritoResponseDTO actualizar(Long id, FavoritoDTO favoritoDTO) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));

        // La lista actual del favorito y la lista destino deben ser del usuario
        cargarListaPropia(favorito.getListaId());
        cargarListaPropia(favoritoDTO.getListaId());

        favorito.setProductoId(favoritoDTO.getProductoId());
        favorito.setNotaPersonal(favoritoDTO.getNotaPersonal());
        favorito.setListaId(favoritoDTO.getListaId());

        Favorito actualizado = favoritoRepository.save(favorito);
        return aResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));
        cargarListaPropia(favorito.getListaId());
        favoritoRepository.deleteById(id);
    }

    /**
     * Exige que la lista exista (404 si no) y pertenezca al usuario
     * autenticado (403 si es ajena y no es ADMIN).
     */
    private void cargarListaPropia(Long listaId) {
        var lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ListaNoEncontradaException(listaId));

        UsuarioAutenticado usuario = UsuarioActual.obtener();
        boolean esDuenio = lista.getUsuarioId() != null && lista.getUsuarioId().equals(usuario.id());
        boolean esAdmin = usuario.rol() == RolUsuario.ADMIN;

        if (!esDuenio && !esAdmin) {
            throw new AccesoDenegadoException("la lista " + listaId);
        }
    }

    private FavoritoResponseDTO aResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getProductoId(),
                favorito.getNotaPersonal(),
                favorito.getFechaCreacion(),
                favorito.getListaId());
    }
}
