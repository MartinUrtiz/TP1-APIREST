package api_blank.producto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catálogo de Productos", description = "Endpoints para consultar el catálogo de productos (consumo de API externa DummyJSON)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los productos", description = "Devuelve una lista paginada de productos del catálogo externo")
    public ResponseEntity<ProductoResponse> obtenerTodos(
            @Parameter(description = "Número de página (empezando en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de productos por página", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        ProductoResponse respuesta = productoService.obtenerTodos(page, limit);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID", description = "Devuelve un producto específico del catálogo externo")
    public ResponseEntity<ProductoDTO> obtenerPorId(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }
}
