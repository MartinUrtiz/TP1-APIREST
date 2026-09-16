package api_blank.producto;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final RestClient restClient;

    private static final String BASE_URL = "https://dummyjson.com";

    public ProductoServiceImpl(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public ProductoResponse obtenerTodos(int page, int limit) {
        int skip = page * limit;

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta = restClient.get()
                .uri("/products?limit={limit}&skip={skip}", limit, skip)
                .retrieve()
                .body(Map.class);

        if (respuesta == null) {
            throw new RuntimeException("No se pudo obtener el catálogo de productos");
        }

        List<Map<String, Object>> productsRaw = (List<Map<String, Object>>) respuesta.get("products");
        int total = (int) respuesta.getOrDefault("total", 0);

        List<ProductoDTO> productos = productsRaw.stream()
                .map(this::mapearProducto)
                .toList();

        return new ProductoResponse(productos, total, skip, limit);
    }

    @Override
    public ProductoDTO obtenerPorId(Long id) {
        @SuppressWarnings("unchecked")
        Map<String, Object> producto = restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(Map.class);

        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }

        return mapearProducto(producto);
    }

    private ProductoDTO mapearProducto(Map<String, Object> raw) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(((Number) raw.get("id")).longValue());
        dto.setNombre((String) raw.get("title"));
        dto.setDescripcion((String) raw.get("description"));
        dto.setPrecio(((Number) raw.get("price")).doubleValue());
        dto.setMarca((String) raw.get("brand"));
        dto.setCategoria((String) raw.get("category"));
        dto.setImagen((String) raw.get("thumbnail"));
        dto.setCalificacion(((Number) raw.get("rating")).doubleValue());
        dto.setStock(((Number) raw.get("stock")).intValue());
        return dto;
    }
}
