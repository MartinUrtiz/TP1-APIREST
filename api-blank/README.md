# TP1 - Spring Boot, API REST y Arquitectura en Capas

## Descripción

API REST que expone dos grupos de endpoints:

- **Catálogo de productos** (`/api/productos`) - Consumo de API externa DummyJSON
- **Favoritos** (`/api/favoritos`) - CRUD completo con persistencia en memoria

## Requisitos

- Java 25
- Maven

## Levantar el proyecto

```bash
./mvnw spring-boot:run
```

O desde tu IDE, ejecutar la clase `ApiBlankApplication`.

El servidor arranca en `http://localhost:8080`

## Documentación

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Endpoints

### Catálogo de Productos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Listar productos (paginado) |
| GET | `/api/productos/{id}` | Obtener producto por ID |

**Parámetros de paginación:**
- `page` (default: 0) - Número de página
- `limit` (default: 10) - Cantidad por página

### Favoritos

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| GET | `/api/favoritos` | 200 OK | Listar todos los favoritos |
| GET | `/api/favoritos/{id}` | 200 OK | Obtener un favorito |
| POST | `/api/favoritos` | 201 Created | Crear un favorito |
| PUT | `/api/favoritos/{id}` | 200 OK | Actualizar un favorito |
| DELETE | `/api/favoritos/{id}` | 204 No Content | Eliminar un favorito |

## Ejemplos de uso con curl

### Productos

```bash
# Listar primeros 5 productos
curl http://localhost:8080/api/productos?limit=5

# Obtener producto con ID 1
curl http://localhost:8080/api/productos/1
```

### Favoritos

```bash
# Crear un favorito
curl -X POST http://localhost:8080/api/favoritos \
  -H "Content-Type: application/json" \
  -d '{"productoId": 1, "notaPersonal": "Lo quiero para Navidad"}'

# Listar favoritos
curl http://localhost:8080/api/favoritos

# Obtener favorito por ID
curl http://localhost:8080/api/favoritos/1

# Actualizar favorito
curl -X PUT http://localhost:8080/api/favoritos/1 \
  -H "Content-Type: application/json" \
  -d '{"productoId": 2, "notaPersonal": "Cambiaste de opinión"}'

# Eliminar favorito
curl -X DELETE http://localhost:8080/api/favoritos/1
```

## Arquitectura

```
src/main/java/api_blank/
├── handler/
│   ├── ErrorResponse.java          # Modelo de respuesta de error
│   └── GlobalExceptionHandler.java # Manejador centralizado (@ControllerAdvice)
├── producto/
│   ├── ProductoDTO.java            # DTO del catálogo externo
│   ├── ProductoResponse.java       # Respuesta paginada
│   ├── ProductoService.java        # Interfaz del servicio
│   ├── ProductoServiceImpl.java    # Implementación (consumo DummyJSON)
│   └── ProductoController.java     # Controller REST
├── favorito/
│   ├── Favorito.java               # Entidad de dominio
│   ├── FavoritoDTO.java            # DTO de entrada
│   ├── FavoritoResponseDTO.java    # DTO de salida
│   ├── FavoritoRepository.java     # Interfaz del repository
│   ├── FavoritoRepositoryImpl.java # Implementación en memoria
│   ├── FavoritoService.java        # Interfaz del servicio
│   ├── FavoritoServiceImpl.java    # Implementación del servicio
│   ├── FavoritoNoEncontradoException.java # Excepción personalizada
│   └── FavoritoController.java     # Controller REST (CRUD)
└── ApiBlankApplication.java        # Clase principal
```
