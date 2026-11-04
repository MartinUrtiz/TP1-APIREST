package api_blank.favorito;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import api_blank.lista.ListaNoEncontradaException;
import api_blank.lista.ListaRepository;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ListaRepository listaRepository;

    public FavoritoServiceImpl(FavoritoRepository favoritoRepository, ListaRepository listaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.listaRepository = listaRepository;
    }

    @Override
    public List<FavoritoResponseDTO> obtenerTodos() {
        return favoritoRepository.findAll().stream()
                .map(this::aResponseDTO)
                .toList();
    }

    @Override
    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));
        return aResponseDTO(favorito);
    }

    @Override
    public FavoritoResponseDTO crear(FavoritoDTO favoritoDTO) {
        validarLista(favoritoDTO.getListaId());

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
        validarLista(favoritoDTO.getListaId());

        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));

        favorito.setProductoId(favoritoDTO.getProductoId());
        favorito.setNotaPersonal(favoritoDTO.getNotaPersonal());
        favorito.setListaId(favoritoDTO.getListaId());

        Favorito actualizado = favoritoRepository.save(favorito);
        return aResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!favoritoRepository.existsById(id)) {
            throw new FavoritoNoEncontradoException(id);
        }
        favoritoRepository.deleteById(id);
    }

    /**
     * El dominio referencia a la lista por id; si la lista no existe, mejor
     * un 404 explícito que una violación de clave foránea (500).
     */
    private void validarLista(Long listaId) {
        if (!listaRepository.existsById(listaId)) {
            throw new ListaNoEncontradaException(listaId);
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
