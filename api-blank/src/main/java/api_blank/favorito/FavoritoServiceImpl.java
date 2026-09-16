package api_blank.favorito;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;

    public FavoritoServiceImpl(FavoritoRepository favoritoRepository) {
        this.favoritoRepository = favoritoRepository;
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
        Favorito favorito = new Favorito();
        favorito.setProductoId(favoritoDTO.getProductoId());
        favorito.setNotaPersonal(favoritoDTO.getNotaPersonal());
        favorito.setFechaCreacion(LocalDateTime.now());

        Favorito guardado = favoritoRepository.save(favorito);
        return aResponseDTO(guardado);
    }

    @Override
    public FavoritoResponseDTO actualizar(Long id, FavoritoDTO favoritoDTO) {
        Favorito favorito = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNoEncontradoException(id));

        favorito.setProductoId(favoritoDTO.getProductoId());
        favorito.setNotaPersonal(favoritoDTO.getNotaPersonal());

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

    private FavoritoResponseDTO aResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.getId(),
                favorito.getProductoId(),
                favorito.getNotaPersonal(),
                favorito.getFechaCreacion());
    }
}
