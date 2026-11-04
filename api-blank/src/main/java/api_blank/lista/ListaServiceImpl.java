package api_blank.lista;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api_blank.favorito.Favorito;
import api_blank.favorito.FavoritoRepository;
import api_blank.favorito.FavoritoResponseDTO;

@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaServiceImpl(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    public List<ListaResponseDTO> obtenerTodas() {
        return listaRepository.findAll().stream()
                .map(this::aResponseDTO)
                .toList();
    }

    @Override
    public ListaResponseDTO obtenerPorId(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new ListaNoEncontradaException(id));
        return aResponseDTO(lista);
    }

    @Override
    public ListaResponseDTO crear(ListaDTO listaDTO) {
        Lista lista = new Lista();
        lista.setNombre(listaDTO.getNombre());

        Lista guardada = listaRepository.save(lista);
        return aResponseDTO(guardada);
    }

    @Override
    public List<FavoritoResponseDTO> obtenerFavoritos(Long id) {
        if (!listaRepository.existsById(id)) {
            throw new ListaNoEncontradaException(id);
        }
        return favoritoRepository.findByListaId(id).stream()
                .map(this::aFavoritoResponse)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new ListaNoEncontradaException(id));

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

        // Primero se valida TODO: si algo falta, no se escribe nada (404)
        Lista origen = listaRepository.findById(origenId)
                .orElseThrow(() -> new ListaNoEncontradaException(origenId));
        Lista destino = listaRepository.findById(destinoId)
                .orElseThrow(() -> new ListaNoEncontradaException(destinoId));

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

    private ListaResponseDTO aResponseDTO(Lista lista) {
        return new ListaResponseDTO(lista.getId(), lista.getNombre());
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
