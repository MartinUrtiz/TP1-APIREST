# TP3 - Seguridad, pruebas e integración continua

Evolución del TP2: misma API, mismos paquetes y arquitectura en capas, ahora con
**autenticación por usuario + JWT**, **autorización por propiedad de recursos y
rol**, **pruebas unitarias y de integración** (Testcontainers) y **CI en GitHub
Actions**, más el despliegue en un proveedor gratuito.

- **Catálogo de productos** (`/api/productos`) - público, sin autenticación: espejo de solo lectura de DummyJSON.
- **Autenticación** (`/api/auth`) - registro (contraseña hasheada con BCrypt) y login que emite un JWT firmado.
- **Favoritos** (`/api/favoritos`) y **Listas** (`/api/listas`) - requieren token; cada usuario opera sólo sobre lo suyo (un ADMIN ve todo).

> Lo que sigue del TP2 (persistencia, migraciones, transacciones) se mantiene
> intacto; las secciones nuevas de este documento cubren seguridad (JWT +
> autorización), tests, CI y despliegue.

## Requisitos

- Java 25
- Maven (o el wrapper `./mvnw`)
- Docker: **obligatorio para los tests de integración** (Testcontainers levanta
  un PostgreSQL real en un contenedor) y recomendado para levantar la base
  local. Sin Docker podés correr sólo los unitarios: `./mvnw test`
- PostgreSQL local: opcional (sólo para correr la aplicación en desarrollo sin
  Docker)

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
 o.f.core.internal.command.DbMigrate : Successfully applied 6 migrations to schema "public", now at version v6
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
 5       | create usuarios           | t
 6       | add usuario id a listas   | t
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

Muestra los grupos: **Autenticación**, Catálogo de Productos, Favoritos y
Listas. Favoritos y Listas llevan el esquema `bearerAuth` en todo el grupo:
para probarlos desde Swagger, hacé login en `POST /api/auth/login` y pegá el
token en el botón **Authorize** (sin el prefijo `Bearer `).

## 4. Endpoints

### Autenticación (nuevo en TP3)

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| POST | `/api/auth/registro` | 201 Created | Registra un usuario (email + contraseña); contraseña guardada como hash BCrypt, rol `USER` |
| POST | `/api/auth/login` | 200 OK | Devuelve `{ token, tipo, id, email, rol }` con un JWT firmado |

Errores: `409 Conflict` si el email ya está registrado; `401 Unauthorized` en
login con credenciales inválidas (mismo mensaje genérico para email inexistente
y contraseña incorrecta, para no revelar quién existe); `400` si el body no
valida (email con formato inválido o contraseña de menos de 6 caracteres).

### Catálogo de Productos (público, sin cambios)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Listar productos (paginado) |
| GET | `/api/productos/{id}` | Obtener producto por ID |

### Favoritos (**requiere token**)

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| GET | `/api/favoritos` | 200 OK | Listar los favoritos de las listas del usuario (ADMIN: todos) |
| GET | `/api/favoritos/{id}` | 200 OK | Obtener un favorito propio |
| POST | `/api/favoritos` | 201 Created | Crear un favorito en una lista propia (requiere `listaId`) |
| PUT | `/api/favoritos/{id}` | 200 OK | Actualizar un favorito propio |
| DELETE | `/api/favoritos/{id}` | 204 No Content | Eliminar un favorito propio |

Errores: `401` sin token (o token inválido/vencido), `403` si la lista del
favorito no pertenece al usuario (salvo ADMIN), `400` si falta `productoId` o
`listaId` (validación), `404` si el favorito o la lista referenciada no existen.

### Listas (**requiere token**)

| Método | Endpoint | Código de éxito | Descripción |
|--------|----------|-----------------|-------------|
| POST | `/api/listas` | 201 Created | Crear una lista (el dueño es el usuario autenticado) |
| GET | `/api/listas` | 200 OK | Listar **las listas del usuario** (todas, si es ADMIN) |
| GET | `/api/listas/{id}` | 200 OK | Obtener una lista propia (o cualquiera, si es ADMIN) |
| GET | `/api/listas/{id}/favoritos` | 200 OK | Favoritos de una lista propia |
| DELETE | `/api/listas/{id}` | 204 No Content | Eliminar una lista **vacía** propia |
| POST | `/api/listas/{origenId}/mover-favoritos` | 200 OK | Mover todos los favoritos de una lista a otra y eliminar la origen (ambas propias) |

Errores nuevos del manejador centralizado:

| Código | Caso |
|--------|------|
| **401 Unauthorized** | Sin token, token inválido o token vencido (cualquier endpoint de favoritos/listas) |
| **403 Forbidden** | La lista existe pero no es del usuario autenticado y no es ADMIN (GET/DELETE lista, favoritos de lista, mover; y en favoritos, cuando la lista es ajena). Nunca 404 por propiedad: no se confunde "no existe" con "no es tuya" |
| 404 | Lista no encontrada (GET/DELETE/mover) o `listaId` inexistente al crear un favorito |
| **409 Conflict** | Intentar borrar una lista que todavía tiene favoritos, o registrarse con un email ya usado |
| 400 | Validación fallida, o mover una lista hacia sí misma |

## 5. Ejemplos con curl

```bash
# 1) Registrarse (201) e iniciar sesión (200 con token)
curl -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"email": "ana@mail.com", "password": "secreto123"}'

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ana@mail.com", "password": "secreto123"}' | jq -r .token)

# 2) Sin token -> 401; con token -> 200
curl -i http://localhost:8080/api/listas
curl -i http://localhost:8080/api/listas -H "Authorization: Bearer $TOKEN"

# 3) Crear listas (el dueño es el usuario del token, no se manda en el body)
curl -X POST http://localhost:8080/api/listas -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"nombre": "Regalos"}'

curl -X POST http://localhost:8080/api/listas -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"nombre": "Para comprar en oferta"}'

# 4) Crear un favorito dentro de una lista propia
curl -X POST http://localhost:8080/api/favoritos -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productoId": 1, "notaPersonal": "Lo quiero para Navidad", "listaId": 2}'

# 5) Favoritos de una lista propia
curl http://localhost:8080/api/listas/2/favoritos -H "Authorization: Bearer $TOKEN"

# 6) Borrar una lista propia vacía (204) o con favoritos (409)
curl -X DELETE http://localhost:8080/api/listas/5 -H "Authorization: Bearer $TOKEN"
curl -X DELETE http://localhost:8080/api/listas/2 -H "Authorization: Bearer $TOKEN"

# 7) Mover todos los favoritos de la lista 1 a la 2 (la 1 se elimina)
curl -X POST http://localhost:8080/api/listas/1/mover-favoritos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"destinoId": 2}'

# 8) Con el token de OTRO usuario: 403 Forbidden (la lista existe, pero no es tuya)
curl -i http://localhost:8080/api/listas/1 -H "Authorization: Bearer $TOKEN_OTRO"
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
│   ├── RestClientConfig.java       # Builder de RestClient (DummyJSON)
│   ├── SecurityConfig.java         # NUEVO TP3: cadena de seguridad (JWT, 401/403, públicos)
│   └── OpenApiConfig.java          # NUEVO TP3: esquema bearerAuth para Swagger
├── auth/                           # NUEVO TP3: autenticación y identidad
│   ├── AuthController.java         # POST /api/auth/registro y /api/auth/login
│   ├── AuthService.java / AuthServiceImpl.java
│   ├── AuthRequestDTO.java / AuthResponseDTO.java
│   ├── JwtService.java             # Emisión/validación de JWT (HS256, id+rol en claims)
│   ├── JwtAuthenticationFilter.java # Lee Authorization: Bearer y llena el SecurityContext
│   ├── UsuarioActual.java          # Lee la identidad autenticada desde el SecurityContext
│   ├── UsuarioAutenticado.java     # Principal (id, email, rol)
│   └── CredencialesInvalidasException (401) / AccesoDenegadoException (403)
├── usuario/                        # NUEVO TP3: dominio, persistencia y registro
│   ├── Usuario.java / RolUsuario.java
│   ├── UsuarioRepository.java      # PUERTO (+findByEmail, existsByEmail)
│   ├── UsuarioRepositoryAdapter.java / UsuarioEntity.java / UsuarioJpaRepository.java
│   ├── UsuarioService.java / UsuarioServiceImpl.java  # BCrypt, 409 si el email existe
│   ├── UsuarioResponseDTO.java
│   └── EmailYaRegistradoException.java (409)
├── handler/
│   ├── ErrorResponse.java          # Modelo de respuesta de error
│   └── GlobalExceptionHandler.java # +401 credenciales, +403 propiedad, +409 email, +404, +400
├── producto/                       # Catálogo externo (público, sin cambios)
├── favorito/                       # + autorización: la lista del favorito debe ser propia
├── lista/                          # + usuarioId (dueño) y autorización en el Service
│   ├── Lista.java                  # Dominio (id, nombre, usuarioId)
│   ├── ListaEntity.java            # @ManyToOne UsuarioEntity (usuario_id, NOT NULL)
│   └── ListaServiceImpl.java       # Acá vive la regla de autorización (ver sección 11)
└── ApiBlankApplication.java

src/main/resources/db/migration/
├── V1__create_favoritos.sql
├── V2__create_listas.sql
├── V3__add_lista_id_a_favoritos.sql
├── V4__lista_id_obligatorio.sql
├── V5__create_usuarios.sql         # NUEVO TP3: usuarios (email único, password_hash, rol)
└── V6__add_usuario_id_a_listas.sql # NUEVO TP3: listas.usuario_id NOT NULL + FK (con backfill)

src/test/java/api_blank/
├── PostgresTestSupport.java        # Contenedor PostgreSQL singleton (Testcontainers) + @DynamicPropertySource
├── ApiITBase.java                  # Base *IT: @SpringBootTest + MockMvc sobre esa base limpia
├── ApiBlankApplicationIT.java      # Contexto completo + /health público + migraciones V5/V6
├── auth/AuthControllerIT.java      # Registro/login: 201/409/200+token/401
├── auth/AuthServiceImplTest.java   # Unitario: login coteja hash y emite JWT con id+rol
├── lista/ListaControllerIT.java    # Propiedad y rol: 200/403/401, ADMIN 200, listas filtradas
├── lista/ListaServiceImplTest.java # Unitario: dueño = usuario autenticado, 403 vs 404
├── favorito/FavoritoControllerIT.java # 401 sin token, 403 en lista ajena, filtrado por dueño
└── usuario/UsuarioServiceImplTest.java # Unitario: NUNCA guarda la contraseña en texto plano
```

## 10. Tests

Hay dos tipos de prueba y **el sufijo del nombre decide qué plugin de Maven la
ejecuta**:

| Sufijo | Plugin | Cuándo | Qué necesita |
|--------|--------|--------|--------------|
| `*Test` | Surefire | `./mvnw test` | Nada: son objetos puros con Mockito (sin Spring ni base) |
| `*IT` | Failsafe | `./mvnw verify` | Docker: Testcontainers levanta un PostgreSQL real y Flyway aplica **todas** las migraciones (V1..V6) sobre esa base limpia en cada corrida |

```bash
./mvnw test      # sólo unitarios (*Test)
./mvnw verify    # unitarios + integración (*IT): ESTO es lo que corre el CI
```

Los tests de integración **no usan el PostgreSQL local ni H2**: cada corrida
levanta su propio contenedor (`PostgresTestSupport`, un singleton por JVM) con
`spring.datasource.*` apuntando adentro del contenedor, así que el resultado es
idéntico en tu máquina y en GitHub Actions (que tampoco tiene PostgreSQL
instalado).

Lo que cubre la suite (nombres en formato `operacion_escenario_resultado`):

- **Unitarios** (`./mvnw test`):
  - `ListaServiceImplTest.crear_lista_quedaAsociadaAlUsuarioAutenticado` — el
    dueño sale del usuario autenticado, nunca del body;
  - `UsuarioServiceImplTest.registrar_nuncaGuardaLaPasswordEnTextoPlano` —
    se guarda un hash BCrypt que coteja contra la contraseña original, y ni el
    hash ni la clave aparecen en la respuesta;
  - más casos de autorización (lista ajena → `AccesoDenegadoException`, no
    existe → `ListaNoEncontradaException`, ADMIN pasa, filtrado de `obtenerTodas`)
    y de login (credenciales válidas/inválidas, token firmado con otro secreto
    o vencido no valida).
- **Integración** (`./mvnw verify`), contra PostgreSQL real + Flyway:
  - `AuthControllerIT` — registro 201 / email repetido 409 / login 200 con
    token / password o email malos 401;
  - `ListaControllerIT` — lista propia 200, lista ajena **403** (no 404),
    sin token **401**, token inválido 401, ADMIN sobre lista ajena **200**,
    `GET /api/listas` devuelve sólo las propias, y lista inexistente 404;
  - `FavoritoControllerIT` — crear/listar/eliminar con propiedad de recursos;
  - `ApiBlankApplicationIT` — el contexto completo levanta, `/health` es
    público y las migraciones V5/V6 quedaron aplicadas en la base limpia.

## 11. Seguridad: cómo funciona y dónde vive la regla de autorización

### El flujo de autenticación

1. `POST /api/auth/registro` guarda el usuario con la contraseña **hasheada
   con BCrypt** (`UsuarioServiceImpl` + `PasswordEncoder` de Spring Security).
   Nunca se guarda ni se devuelve la contraseña en texto plano.
2. `POST /api/auth/login` coteja la contraseña contra el hash y, si coincide,
   emite un JWT firmado con HMAC-SHA256 (`JwtService`): el **id** del usuario
   va en `sub` y el **email** y el **rol** van como claims.
3. En cada request, `JwtAuthenticationFilter` lee
   `Authorization: Bearer <token>`, valida firma y expiración, y deja el
   `UsuarioAutenticado` en el `SecurityContext`. Si no hay header o el token
   es inválido/vencido, **no deja identidad** y la cadena de seguridad responde
   **401** desde el `authenticationEntryPoint` (`SecurityConfig`).
4. `SecurityConfig` declara públicos `/api/auth/**`, `/api/productos/**` y
   `/health`; todo lo demás exige token. Sesión `STATELESS`: no hay cookie ni
   sesión del lado del servidor, la identidad vive sólo en cada JWT.

### ¿Dónde vive la regla de autorización? (consigna 4) — en el **Service**

Concretamente en `ListaServiceImpl.cargarListaPropia(...)` (y su equivalente
`FavoritoServiceImpl.cargarListaPropia(...)`), **no** en el Controller, ni en
un filtro, ni en anotaciones. Por qué:

- **El filtro JWT resuelve identidad, no propiedad.** El filtro sabe *quién*
  sos, pero no *qué* recurso estás pidiendo: no puede decidir si la lista 7 es
  tuya sin meterse en la lógica de negocio.
- **El Service es la única puerta a los datos.** Todos los Controllers
  delegan en Services (`Controller → Service → puerto → adapter`), así que
  cualquier endpoint —los de hoy y los que se agreguen— queda cubierto por la
  misma regla, sin acoplar seguridad a cada método anotado.
- **El dominio está ahí.** La propiedad es `Lista.usuarioId`: la capa que
  conoce el dominio es la que puede compararla con `UsuarioActual.id()` (que
  a su vez lee el `SecurityContext` que llenó el filtro JWT).
- **Es testeable sin HTTP ni Spring Security.** `ListaServiceImplTest` verifica
  la regla con un mock del repositorio y un `SecurityContext` a mano; una
  anotación `@PreAuthorize` exigiría levantar el contexto completo.
- **El dueño al crear también sale del token, nunca del body**: `crear()` usa
  `UsuarioActual.id()` y descarta cualquier id de dueño que mande el cliente.

La misma lógica se aplica a los favoritos: un favorito cuelga de una lista y
la lista debe ser del usuario autenticado (`Usuario → Lista → Favorito`).

**Respuestas:** si el recurso **no existe** → `404` (`ListaNoEncontradaException`);
si **existe pero no es del usuario** (y no es ADMIN) → `403`
(`AccesoDenegadoException`), nunca 404, para no confundir "no existe" con "no
es tuya". Un **ADMIN** salta la verificación de dueño y puede leer/listar
cualquier lista. Todo eso está manejado en `GlobalExceptionHandler` (401, 403,
409, 404, 400).

### Configuración por variables de entorno

Nada sensible está fijo en `application.properties`; todos los valores usan
`${VARIABLE:valorPorDefecto}` para que funcione en local sin configurar nada:

| Variable | Para qué | Defecto en local |
|----------|----------|------------------|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Conexión a PostgreSQL | `localhost:5432/api_blank`, `api_blank` |
| `JWT_SECRET` | Secreto para firmar los JWT (≥ 32 bytes) | valor de desarrollo (cambiar en producción) |
| `JWT_EXPIRATION_MS` | Vigencia del token | `3600000` (1 hora) |

## 12. Integración continua (GitHub Actions)

El workflow está en [`.github/workflows/ci.yml`](../.github/workflows/ci.yml)
(en la raíz del repositorio, no en `api-blank/`). En **cada push** (y cada PR)
corre en un runner limpio de Ubuntu:

1. checkout + JDK 25 (Temurin) con caché de Maven;
2. `./mvnw verify` desde `api-blank/`: compila y ejecuta **Surefire**
   (`*Test`) y **Failsafe** (`*IT`). Los `*IT` levantan su PostgreSQL con
   Testcontainers y aplican las migraciones contra esa base limpia: el CI no
   depende de ninguna base externa.

**Cómo leer el resultado:** en la pestaña *Actions* del repo, cada corrida
muestra el job `Unit + integration tests` con un ✓ verde o una ✗ roja. Al
fallar, el log del paso *Build + Surefire + Failsafe* muestra el resumen
(`Tests run: N, Failures: X, Errors: Y`) y, para cada prueba, la aserción que
explotó; Maven también deja los informes en `api-blank/target/surefire-reports`
y `api-blank/target/failsafe-reports`. Se verificó el pipeline en rojo (test
roto a propósito: el job falla) y en verde al arreglarlo.

Detalle importante: el wrapper `api-blank/mvnw` debe tener permiso de
ejecución en el repo (`git update-index --chmod=+x api-blank/mvnw`), si no el
paso de Maven falla con `Permission denied`.

## 13. Despliegue en Render

La aplicación se despliega con el `Dockerfile` de la raíz del repositorio (build
en el proveedor: `./mvnw package -DskipTests` y arranque con JRE 25). Pasos
seguidos:

1. **Crear la cuenta y la base**: en [render.com](https://render.com) → *New →
   PostgreSQL* (plan Free). Anotá `External Database URL`, usuario y contraseña.
2. **Crear el servicio**: *New → Web Service* → conectar el repo
   `MartinUrtiz/TP1-APIREST` → Render detecta el `render.yaml` del repositorio
   (blueprint) o configurá a mano:
   - Runtime: **Docker**, Dockerfile `./Dockerfile`, raíz `.`;
   - Plan **Free**; *Health Check Path*: `/health`.
3. **Variables de entorno** (Settings → Environment):
   - `DB_URL` = `jdbc:postgresql://<host>:5432/<db>` (el `External Database URL`
     de Render viene como `postgres://user:pass@host:5432/db`: convertilo a
     formato JDBC y `DB_USER`/`DB_PASSWORD` salen de ahí);
   - `DB_USER`, `DB_PASSWORD`;
   - `JWT_SECRET` = un string aleatorio de ≥ 32 caracteres (en el blueprint
     Render lo genera solo con `generateValue: true`);
   - `JWT_EXPIRATION_MS` = `3600000`.
4. **Deploy**: *Deploy*. En los logs del servicio tenés que ver:
   - `Successfully applied 6 migrations ... now at version v6` (Flyway corrió
     las migraciones **en la base remota**, incluidas V5 y V6);
   - arranque sin errores (`Started ApiBlankApplication ...`) y
     `Tomcat started on port 8080`.
5. **Verificación pública**:
   - `GET https://<servicio>.onrender.com/health` → `200` **sin token** (público);
   - `GET https://<servicio>.onrender.com/api/listas` **sin token** → `401`;
   - registrarse, loguearse y repetir el GET **con** `Authorization: Bearer
     <token>` → `200` con las listas del usuario.
6. El servicio puede quedar suspendido después (el plan Free se duerme): la
   evidencia queda en los logs de la corrida y en las capturas de los pasos 4
   y 5. Variables de entorno y base quedan configuradas para levantarlo de
   nuevo cuando haga falta.

## Evidencia (TP2)

- Colección de requests probados (éxito y error, por recurso nuevo): [`requests.http`](requests.http)
- Historial de migraciones aplicadas: tabla `flyway_schema_history` (4 filas, `success = t`)
- **Migración sobre datos previos**: existía un favorito creado cuando sólo había V1; al arrancar con V2-V4, Flyway lo reasignó a la lista "Sin clasificar" y `lista_id` pasó a `NOT NULL` sin perder filas.
- **Inmutabilidad de las migraciones**: se editó `V1` y la aplicación no arrancó (`Migration checksum mismatch for migration version 1`); se revirtió el cambio y volvió a arrancar normal.
- **Atomicidad de `mover-favoritos`**: se forzó el fallo del `DELETE` con un trigger en la base; la transacción hizo rollback y los `UPDATE` de reasignación previos quedaron deshechos (el favorito siguió en su lista origen).
- Casos probados contra PostgreSQL real: CRUD de favoritos, alta/baja/consulta de listas, `409` al borrar una lista con favoritos, `404`/`400` en mover favoritos, y el movimiento completo (favoritos reasignados + origen eliminado) en una sola transacción. Los datos sobreviven a un reinicio de la aplicación.

## Evidencia (TP3)

- **Suite en verde localmente**: `./mvnw verify` → unitarios (Surefire) + integración (Failsafe) con Testcontainers y Flyway sobre PostgreSQL limpio.
- **CI en GitHub Actions**: corrida exitosa del workflow `CI` en cada push (link en la sección 12); verificado también en rojo con un test roto a propósito y en verde al arreglarlo.
- **Migraciones V5 y V6 sin ediciones** posteriores a aplicadas (Flyway valida checksums en cada arranque; en CI corren contra base nueva en cada corrida).
- **Endpoints protegidos**: `/api/listas` y `/api/favoritos` responden `401` sin token (o con token inválido/vencido) y `200` con uno válido; lista ajena `403`, ADMIN sobre lista ajena `200`.
- **Despliegue**: URL pública con `GET /health` respondiendo `200` sin token, logs con las 6 migraciones aplicadas en la base remota y arranque sin errores (sección 13).
