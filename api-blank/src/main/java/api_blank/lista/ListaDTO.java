package api_blank.lista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO de entrada para crear una lista")
public class ListaDTO {

    @NotBlank(message = "El nombre de la lista es obligatorio")
    @Size(max = 100, message = "El nombre de la lista no puede superar los 100 caracteres")
    @Schema(description = "Nombre de la lista", example = "Regalos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    public ListaDTO() {
    }

    public ListaDTO(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
