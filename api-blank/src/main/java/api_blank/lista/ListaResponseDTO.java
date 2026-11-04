package api_blank.lista;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de salida para una lista")
public class ListaResponseDTO {

    @Schema(description = "ID de la lista", example = "1")
    private Long id;

    @Schema(description = "Nombre de la lista", example = "Regalos")
    private String nombre;

    public ListaResponseDTO() {
    }

    public ListaResponseDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
