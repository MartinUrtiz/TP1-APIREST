package api_blank.favorito;

import java.util.List;

public interface FavoritoService {

    List<FavoritoResponseDTO> obtenerTodos();

    FavoritoResponseDTO obtenerPorId(Long id);

    FavoritoResponseDTO crear(FavoritoDTO favoritoDTO);

    FavoritoResponseDTO actualizar(Long id, FavoritoDTO favoritoDTO);

    void eliminar(Long id);
}
