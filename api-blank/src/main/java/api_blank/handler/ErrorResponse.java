package api_blank.handler;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error uniforme para toda la API")
public class ErrorResponse {

    @Schema(description = "Código HTTP del error", example = "400")
    private int status;

    @Schema(description = "Mensaje descriptivo del error", example = "Validación fallida")
    private String mensaje;

    @Schema(description = "Timestamp del error")
    private LocalDateTime timestamp;

    @Schema(description = "Detalles de los errores de validación (opcional)")
    private Map<String, String> detalles;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String mensaje, Map<String, String> detalles) {
        this.status = status;
        this.mensaje = mensaje;
        this.detalles = detalles;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getDetalles() {
        return detalles;
    }

    public void setDetalles(Map<String, String> detalles) {
        this.detalles = detalles;
    }
}
