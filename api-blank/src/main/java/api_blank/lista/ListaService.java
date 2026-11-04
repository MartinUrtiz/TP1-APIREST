package api_blank.lista;

import java.util.List;

import api_blank.favorito.FavoritoResponseDTO;

public interface ListaService {

    List<ListaResponseDTO> obtenerTodas();

    ListaResponseDTO obtenerPorId(Long id);

    ListaResponseDTO crear(ListaDTO listaDTO);

    List<FavoritoResponseDTO> obtenerFavoritos(Long id);

    void eliminar(Long id);

    MoverFavoritosResponseDTO moverFavoritos(Long origenId, MoverFavoritosDTO moverDTO);
}
