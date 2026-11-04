package api_blank.lista;

/**
 * Se lanza al intentar eliminar una lista que todavía tiene favoritos
 * asociados: la API responde 409 Conflict en lugar de un 500 por
 * violación de la clave foránea.
 */
public class ListaConFavoritosException extends RuntimeException {

    public ListaConFavoritosException(Long id) {
        super("La lista con id " + id + " todavía tiene favoritos asociados: movelos o elimínalos antes de borrar la lista");
    }
}
