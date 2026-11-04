package api_blank.auth;

/**
 * Acceso denegado por propiedad de recurso: el usuario no es dueño del
 * recurso y no es ADMIN -> 403 Forbidden (nunca 404, para no confundir
 * "no existe" con "no es tuya").
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String recurso) {
        super("Acceso denegado: " + recurso + " no te pertenece (ni sos ADMIN)");
    }
}
