package api_blank.lista;

public class ListaNoEncontradaException extends RuntimeException {

    public ListaNoEncontradaException(Long id) {
        super("Lista no encontrada con id: " + id);
    }
}
