package api_blank.lista;

/**
 * Mover favoritos de una lista a sí misma dejaría los favoritos apuntando a
 * una lista recién borrada. Se responde 400 Bad Request.
 */
public class ListasIgualesException extends RuntimeException {

    public ListasIgualesException(Long id) {
        super("La lista origen y la lista destino no pueden ser la misma (id " + id + ")");
    }
}
