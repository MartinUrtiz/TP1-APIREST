package api_blank.producto;

import java.util.List;

public interface ProductoService {

    ProductoResponse obtenerTodos(int page, int limit);

    ProductoDTO obtenerPorId(Long id);
}
