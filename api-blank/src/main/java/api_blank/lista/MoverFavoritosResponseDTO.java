package api_blank.lista;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de mover todos los favoritos de una lista a otra")
public class MoverFavoritosResponseDTO {

    @Schema(description = "ID de la lista origen (eliminada)", example = "1")
    private Long origenId;

    @Schema(description = "ID de la lista destino", example = "2")
    private Long destinoId;

    @Schema(description = "Nombre de la lista origen", example = "Compras viejas")
    private String nombreOrigen;

    @Schema(description = "Nombre de la lista destino", example = "Regalos")
    private String nombreDestino;

    @Schema(description = "Cantidad de favoritos reasignados", example = "3")
    private int favoritosMovidos;

    public MoverFavoritosResponseDTO() {
    }

    public MoverFavoritosResponseDTO(Long origenId, Long destinoId, String nombreOrigen, String nombreDestino,
            int favoritosMovidos) {
        this.origenId = origenId;
        this.destinoId = destinoId;
        this.nombreOrigen = nombreOrigen;
        this.nombreDestino = nombreDestino;
        this.favoritosMovidos = favoritosMovidos;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }

    public String getNombreOrigen() {
        return nombreOrigen;
    }

    public void setNombreOrigen(String nombreOrigen) {
        this.nombreOrigen = nombreOrigen;
    }

    public String getNombreDestino() {
        return nombreDestino;
    }

    public void setNombreDestino(String nombreDestino) {
        this.nombreDestino = nombreDestino;
    }

    public int getFavoritosMovidos() {
        return favoritosMovidos;
    }

    public void setFavoritosMovidos(int favoritosMovidos) {
        this.favoritosMovidos = favoritosMovidos;
    }
}
