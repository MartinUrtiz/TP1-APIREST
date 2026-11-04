package api_blank.favorito;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "DTO de entrada para crear o actualizar un favorito")
public class FavoritoDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    @Schema(description = "ID del producto externo referenciado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productoId;

    @Schema(description = "Nota personal del usuario", example = "Lo quiero para Navidad")
    private String notaPersonal;

    @NotNull(message = "El ID de la lista es obligatorio")
    @Positive(message = "El ID de la lista debe ser un número positivo")
    @Schema(description = "ID de la lista a la que pertenece el favorito", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long listaId;

    public FavoritoDTO() {
    }

    public FavoritoDTO(Long productoId, String notaPersonal, Long listaId) {
        this.productoId = productoId;
        this.notaPersonal = notaPersonal;
        this.listaId = listaId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNotaPersonal() {
        return notaPersonal;
    }

    public void setNotaPersonal(String notaPersonal) {
        this.notaPersonal = notaPersonal;
    }

    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }
}
