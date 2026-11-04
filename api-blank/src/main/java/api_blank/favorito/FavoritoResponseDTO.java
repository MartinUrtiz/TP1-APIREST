package api_blank.favorito;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de salida para un favorito")
public class FavoritoResponseDTO {

    @Schema(description = "ID del favorito", example = "1")
    private Long id;

    @Schema(description = "ID del producto externo referenciado", example = "1")
    private Long productoId;

    @Schema(description = "Nota personal del usuario", example = "Lo quiero para Navidad")
    private String notaPersonal;

    @Schema(description = "Fecha y hora en que se agregó como favorito")
    private LocalDateTime fechaCreacion;

    @Schema(description = "ID de la lista a la que pertenece el favorito", example = "1")
    private Long listaId;

    public FavoritoResponseDTO() {
    }

    public FavoritoResponseDTO(Long id, Long productoId, String notaPersonal, LocalDateTime fechaCreacion,
            Long listaId) {
        this.id = id;
        this.productoId = productoId;
        this.notaPersonal = notaPersonal;
        this.fechaCreacion = fechaCreacion;
        this.listaId = listaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getListaId() {
        return listaId;
    }

    public void setListaId(Long listaId) {
        this.listaId = listaId;
    }
}
