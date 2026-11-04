# TP2 - PostgreSQL, JPA/Hibernate y Flyway

Continuación del TP1: misma API, misma arquitectura en capas, pero con la
persistencia de **Favoritos** y la nueva entidad **Listas** respaldadas por
**PostgreSQL** en lugar de un `Map` en memoria, con el esquema versionado por
**Flyway**.

- **Catálogo de productos** (`/api/productos`) - no cambió: sigue siendo un espejo de solo lectura de DummyJSON, sin persistencia propia.
- **Favoritos** (`/api/favoritos`) - CRUD completo persistido en PostgreSQL a través de JPA/Hibernate.
- **Listas** (`/api/listas`) - entidad nueva que organiza los favoritos (cada favorito pertenece a una lista), con la operación transaccional de mover favoritos entre listas.

## Requisitos

- Java 25
- Maven (o el wrapper `./mvnw`)
- Docker **o** una instalación local de PostgreSQL 14+

## 1. Levantar PostgreSQL

### Opción A: Docker (recomendada)

En la raíz del proyecto (`api-blank/`) hay un `docker-compose.yml` con la
imagen oficial `postgres:17-alpine`. La base, el usuario y la contraseña se
definen por variables de entorno (con valores por defecto que coinciden con
`application.properties`):

```bash
docker compose up -d
```

Esto levanta PostgreSQL en `localhost:5432` con:

| Variable | Valor por defecto |
|----------|-------------------|
| `POSTGRES_DB` | `api_blank` |
| `POSTGRES_USER` | `api_blank` |
| `POSTGRES_PASSWORD` | `api_blank123` |

Para cambiarlos: `POSTGRES_DB=otra_db docker compose up -d` (y pasar las
mismas variables a la aplicación, ver abajo).

### Opción B: PostgreSQL local (sin Docker)

1. Instalá PostgreSQL 14+ (por ejemplo con `winget install PostgreSQL.PostgreSQL.17`, o los binarios de EDB).
2. Creá el usuario y la base con los mismos datos que usa la aplicación:

```bash
createuser -s api_blank
createdb -O api_blank api_blank
```

3. Si usás otros datos de conexión, sobrescribilos con variables de entorno:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/mi_base"
export DB_USER="mi_usuario"
export DB_PASSWORD="mi_password"
```

## 2. Levantar el proyecto y verificar las migraciones

```bash
./mvnw spring-boot:run
```

O desde tu IDE, ejecutando `ApiBlankApplication`. El servidor arranca en
`http://localhost:8080`.

**Flyway aplica las migraciones pendientes solo, antes de que Hibernate
arranque.** En el log tenés que ver algo así:

```
o.f.core.internal.command.DbMigrate : Current version of schema "public": 0
o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "1 - create favoritos"
o.f.core.internal.command.DbMigrate : Successfully applied 4 migrations to schema "public", now at version v4
```

Y en la base, la tabla `flyway_schema_history` que Flyway crea sola:

```bash
psql -U api_blank -d api_blank -c "SELECT version, description, success FROM flyway_schema_history;"
```

```
 version |        description        | success
---------+---------------------------+---------
 1       | create favoritos          | t
 2       | create listas             | t
 3       | add lista id a favoritos  | t
 4       | lista id obligatorio      | t
```

Si el esquema no coincide con las entidades, la aplicación **no arranca**: el
`spring.jpa.hibernate.ddl-auto=validate` falla con un error tipo
`missing table [...]`. Ese es el síntoma típico de haber agregado
`flyway-core` a secas sin `spring-boot-starter-flyway`: en Spring Boot 4.x la
autoconfiguración de Flyway vive en el starter, y sin él la app arranca sin
migrar nada.

**Las migraciones son inmutables una vez aplicadas.** Flyway guarda el
checksum de cada archivo en `flyway_schema_history` y lo valida en cada
arranque: si editás una migración ya aplicada, la aplicación falla con
`Migration checksum mismatch for migration version N` (esto se verificó
editando `V1` y observando el error). Por eso, cualquier cambio de esquema se
expresa como una **migración nueva** (ver sección "Evolución del esquema").

## 3. Documentación

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs (OpenAPI): http://localhost:8080/api-docs

Muestra los tres grupos: Catálogo de Productos, Favoritos y Listas.

## 4. Endpoints

### Catálogo de Productos (sin cambios)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Listar productos (paginado) |
| GET | `/api/productos/{id}` | Obtener producto por ID |

### Favoritos

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| GET | `/api/favoritos` | 200 OK | Listar todos los favoritos |
| GET | `/api/favoritos/{id}` | 200 OK | Obtener un favorito |
| POST | `/api/favoritos` | 201 Created | Crear un favorito (requiere `listaId`) |
| PUT | `/api/favoritos/{id}` | 200 OK | Actualizar un favorito |
| DELETE | `/api/favoritos/{id}` | 204 No Content | Eliminar un favorito |

Errores: `400` si falta `productoId` o `listaId` (validación), `404` si el
favorito o la lista referenciada no existen.

### Listas

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| POST | `/api/listas` | 201 Created | Crear una lista |
| GET | `/api/listas` | 200 OK | Listar listas |
| GET | `/api/listas/{id}` | 200 OK | Obtener una lista |
| GET | `/api/listas/{id}/favoritos` | 200 OK | Favoritos de una lista |
| DELETE | `/api/listas/{id}` | 204 No Content | Eliminar una lista **vacía** |
| POST | `/api/listas/{origenId}/mover-favoritos` | 200 OK | Mover todos los favoritos de una lista a otra y eliminar la origen |

Errores nuevos del manejador centralizado:

| Código | Caso |
|--------|------|
| 404 | Lista no encontrada (GET/DELETE/mover) o `listaId` inexistente al crear un favorito |
| **409 Conflict** | Intentar borrar una lista que todavía tiene favoritos |
| 400 | Validación fallida, o mover una lista hacia sí misma |

## 5. Ejemplos con curl

```bash
# Crear listas
curl -X POST http://localhost:8080/api/listas -H "Content-Type: application/json" \
  -d '{"nombre": "Regalos"}'

curl -X POST http://localhost:8080/api/listas -H "Content-Type: application/json" \
  -d '{"nombre": "Para comprar en oferta"}'

# Crear un favorito dentro de una lista
curl -X POST http://localhost:8080/api/favoritos -H "Content-Type: application/json" \
  -d '{"productoId": 1, "notaPersonal": "Lo quiero para Navidad", "listaId": 2}'

# Favoritos de una lista
curl http://localhost:8080/api/listas/2/favoritos

# Borrar una lista vacía (204) o con favoritos (409)
curl -X DELETE http://localhost:8080/api/listas/5
curl -X DELETE http://localhost:8080/api/listas/2

# Mover todos los favoritos de la lista 1 a la 2 (la 1 se elimina)
curl -X POST http://localhost:8080/api/listas/1/mover-favoritos \
  -H "Content-Type: application/json" -d '{"destinoId": 2}'
```

La colección completa de requests (éxito y error, incluida la operación de
mover favoritos) está en [`requests.http`](requests.http).

## 6. Qué cambió respecto del TP1 en la capa de persistencia

`FavoritoRepository` es un **puerto**: un contrato que define qué operaciones
de persistencia necesita el negocio, sin decir cómo se implementan. Tanto el
adapter en memoria del TP1 como el `FavoritoRepositoryAdapter` de este TP son
**adapters intercambiables** detrás de esa misma interfaz. Por eso, al
reemplazar la implementación, las clases de afuera quedaron intactas:

| Clase | ¿Qué pasó? | Por qué |
|-------|------------|---------|
| `FavoritoRepository` (puerto) | Se mantuvo; en el punto 5 se sumaron `findByListaId` y `existsByListaId` | Sigue siendo el contrato; los métodos nuevos son requisito de la relación con listas |
| `FavoritoRepositoryImpl` (adapter en memoria) | **Eliminado** | Dos beans implementando el mismo puerto serían ambiguos para Spring |
| `FavoritoRepositoryAdapter` (adapter JPA) | **Nuevo** | Convierte `FavoritoEntity` ↔ `Favorito` y delega en `FavoritoJpaRepository` |
| `FavoritoEntity`, `FavoritoJpaRepository` | **Nuevas** | Infraestructura JPA: mapeo de tabla y repositorio que Spring Data implementa solo |
| `Favorito` (dominio) | **Idéntico en el punto 3**; en el punto 5 se le sumó `listaId` | El dominio no conoce JPA: un record/POJO plano sigue siendo un favorito |
| `FavoritoService` (interfaz) | **Idéntica** | El contrato de negocio no cambió |
| `FavoritoServiceImpl` | **Idéntico en el punto 3**; en el punto 5 se le sumó el mapeo de `listaId` y la validación de que la lista exista | La lógica de negocio es la misma; cambió sólo lo que exige el nuevo requisito |
| `FavoritoController` | **Idéntico en el punto 3**; en el punto 5 no cambió (el `listaId` llega en el DTO) | El controller no sabe dónde ni cómo se guarda |
| `FavoritoDTO` / `FavoritoResponseDTO` | **Idénticos en el punto 3**; en el punto 5 se les sumó `listaId` (con `@NotNull` en el de entrada) | Son el contrato HTTP; la persistencia no les importa |

> Los DTOs conservan los nombres del TP1 (`FavoritoDTO` y
> `FavoritoResponseDTO`), que son los que el práctico nombra como
> `FavoritoRequest` y `FavoritoResponse`.

**Por qué fue posible:** el flujo es `Controller → Service → puerto
Repository → adapter`. El Service depende de una interfaz, no de una clase
concreta. Cambiar `ConcurrentHashMap` por PostgreSQL fue tocar el adapter (y
agregar entidades/migraciones), sin tocar el dominio, el contrato del Service
ni el Controller: eso es justamente la ventaja de separar puertos de
adaptadores.

Otros ajustes de configuración (no son clases, pero forman parte del cambio):

- `spring.jpa.hibernate.ddl-auto=validate` → el esquema lo maneja Flyway; Hibernate sólo lo verifica.
- `spring.jpa.open-in-view=false` → no se mantiene la sesión JPA abierta durante toda la petición; el adapter convierte a dominio dentro de su propia operación (la asociación a `lista` es `@ManyToOne` EAGER, así que no hace falta una sesión abierta para leerla).

## 7. Evolución del esquema (por qué V4 y no editar V1/V3)

Flyway guarda el checksum de cada migración ya aplicada en
`flyway_schema_history` y lo valida en cada arranque: editar `V1`, `V2` o
`V3` rompe la validación (`Migration checksum mismatch`) y, en una base con
datos reales, el DDL ya ejecutado no se repite igual. El caso de `lista_id`
(se agregó nullable en V3 y recién en V4 se pone `NOT NULL` tras reasignar
los favoritos previos a la lista "Sin clasificar") se resuelve con una
migración nueva porque es una evolución sobre el estado que ya dejó V3, y
así corre tanto en bases nuevas como en bases con datos del punto 3.

## 8. Transacciones: por qué `@Transactional` en mover-favoritos

`POST /api/listas/{origenId}/mover-favoritos` ejecuta **dos escrituras**:
reasignar todos los favoritos de la lista origen a la destino y eliminar la
lista origen. Ambas están en `ListaServiceImpl.moverFavoritos`, marcado con
`@Transactional`.

Esto es la **atomicidad (A) de ACID**: la unidad de trabajo es "el movimiento
completo" y debe aplicarse **todo o nada**. Sin `@Transactional`, cada
`save`/`delete` se ejecuta en su propia transacción (autocommit) y se confirma
por separado. Si una escritura falla a mitad de camino después de que otra ya
se confirmó, la base queda en un estado que ningún pedido pidió:

- Si los `UPDATE` avanzan a medias y luego falla algo, quedarían unos favoritos movidos y otros en la lista origen: a medias.
- Si los `UPDATE` se confirman y después falla el `DELETE`, el cliente recibe un error y cree que no pasó nada, pero los favoritos ya están en la destino y la lista origen quedó vacía (o al revés, según el orden): el estado no corresponde a lo que la API reportó.
- Sin clave foránea, peor aún: favoritos apuntando a un id de lista inexistente rompería la **integridad referencial (I)**.

Con `@Transactional`, si cualquier escritura lanza una excepción, Spring
hace **rollback** de todas las escrituras de esa misma transacción y la base
vuelve al estado anterior: o el movimiento entero queda aplicado (y el origen
borrado), o no cambió nada. La atomicidad además garantiza que otras
transacciones concurrentes no vean el estado intermedio.

## 9. Arquitectura

```
src/main/java/api_blank/
├── config/
│   └── RestClientConfig.java       # Builder de RestClient (DummyJSON)
├── handler/
│   ├── ErrorResponse.java          # Modelo de respuesta de error
│   └── GlobalExceptionHandler.java # Manejador centralizado (+404 listas, +409 conflicto, +400)
├── producto/                       # Catálogo externo (sin cambios)
│   ├── ProductoDTO.java
│   ├── ProductoResponse.java
│   ├── ProductoService.java
│   ├── ProductoServiceImpl.java
│   └── ProductoController.java
├── favorito/
│   ├── Favorito.java               # Dominio (+listaId en el punto 5)
│   ├── FavoritoDTO.java            # DTO de entrada (+listaId @NotNull)
│   ├── FavoritoResponseDTO.java    # DTO de salida (+listaId)
│   ├── FavoritoRepository.java     # PUERTO (contrato)
│   ├── FavoritoRepositoryAdapter.java # ADAPTER JPA (reemplaza al en memoria)
│   ├── FavoritoEntity.java         # Entidad JPA -> tabla favoritos
│   ├── FavoritoJpaRepository.java  # JpaRepository (Spring Data lo implementa)
│   ├── FavoritoService.java
│   ├── FavoritoServiceImpl.java
│   ├── FavoritoNoEncontradoException.java
│   └── FavoritoController.java
├── lista/                          # Módulo nuevo
│   ├── Lista.java                  # Dominio (id, nombre)
│   ├── ListaDTO.java / ListaResponseDTO.java
│   ├── MoverFavoritosDTO.java / MoverFavoritosResponseDTO.java
│   ├── ListaRepository.java        # PUERTO (misma forma que FavoritoRepository)
│   ├── ListaRepositoryAdapter.java # ADAPTER JPA
│   ├── ListaEntity.java            # Entidad JPA -> tabla listas
│   ├── ListaJpaRepository.java
│   ├── ListaService.java
│   ├── ListaServiceImpl.java       # @Transactional en moverFavoritos
│   ├── ListaController.java
│   └── ListaNoEncontradaException.java / ListaConFavoritosException.java / ListasIgualesException.java
└── ApiBlankApplication.java

src/main/resources/db/migration/
├── V1__create_favoritos.sql
├── V2__create_listas.sql
├── V3__add_lista_id_a_favoritos.sql
└── V4__lista_id_obligatorio.sql
```

## 10. Tests

```bash
./mvnw test
```

Los tests usan H2 en memoria (sólo `test` scope): no necesitan PostgreSQL
levantado. La aplicación real siempre corre contra PostgreSQL.

## Evidencia

- Colección de requests probados (éxito y error, por recurso nuevo): [`requests.http`](requests.http)
- Historial de migraciones aplicadas: tabla `flyway_schema_history` (4 filas, `success = t`)
- **Migración sobre datos previos**: existía un favorito creado cuando sólo había V1; al arrancar con V2-V4, Flyway lo reasignó a la lista "Sin clasificar" y `lista_id` pasó a `NOT NULL` sin perder filas.
- **Inmutabilidad de las migraciones**: se editó `V1` y la aplicación no arrancó (`Migration checksum mismatch for migration version 1`); se revirtió el cambio y volvió a arrancar normal.
- **Atomicidad de `mover-favoritos`**: se forzó el fallo del `DELETE` con un trigger en la base; la transacción hizo rollback y los `UPDATE` de reasignación previos quedaron deshechos (el favorito siguió en su lista origen).
- Casos probados contra PostgreSQL real: CRUD de favoritos, alta/baja/consulta de listas, `409` al borrar una lista con favoritos, `404`/`400` en mover favoritos, y el movimiento completo (favoritos reasignados + origen eliminado) en una sola transacción. Los datos sobreviven a un reinicio de la aplicación.
