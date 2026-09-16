package api_blank.favorito;

public class FavoritoNoEncontradoException extends RuntimeException {

    public FavoritoNoEncontradoException(Long id) {
        super("Favorito no encontrado con id: " + id);
    }
}
