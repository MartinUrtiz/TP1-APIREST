package api_blank.producto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta paginada de productos")
public class ProductoResponse {

    @Schema(description = "Lista de productos")
    private List<ProductoDTO> productos;

    @Schema(description = "Cantidad total de productos disponibles", example = "100")
    private int total;

    @Schema(description = "Índice de la página actual", example = "0")
    private int skip;

    @Schema(description = "Cantidad de productos por página", example = "10")
    private int limit;

    public ProductoResponse() {
    }

    public ProductoResponse(List<ProductoDTO> productos, int total, int skip, int limit) {
        this.productos = productos;
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }

    public List<ProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoDTO> productos) {
        this.productos = productos;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
