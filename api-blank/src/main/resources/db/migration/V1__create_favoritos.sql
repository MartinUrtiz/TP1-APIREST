-- V1: tabla de favoritos (persistencia propia de la aplicación).
-- Los productos del catálogo NO se persisten: siguen siendo un espejo de solo
-- lectura de la API externa.
CREATE TABLE favoritos (
    id          BIGSERIAL PRIMARY KEY,
    producto_id BIGINT       NOT NULL,
    nota        VARCHAR(255),
    fecha_alta  TIMESTAMP    NOT NULL
);
