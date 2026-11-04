package api_blank.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "DTO de entrada para mover los favoritos de una lista a otra")
public class MoverFavoritosDTO {

    @NotNull(message = "El ID de la lista destino es obligatorio")
    @Positive(message = "El ID de la lista destino debe ser un número positivo")
    @Schema(description = "ID de la lista destino", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long destinoId;

    public MoverFavoritosDTO() {
    }

    public MoverFavoritosDTO(Long destinoId) {
        this.destinoId = destinoId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public void setDestinoId(Long destinoId) {
        this.destinoId = destinoId;
    }
}
