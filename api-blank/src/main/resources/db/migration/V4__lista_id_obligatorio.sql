-- V4: evolución del esquema sobre datos existentes.
-- Los favoritos creados antes del punto 5 no tienen lista: en lugar de editar
-- V1/V3 (ya aplicadas e inmutables), se agrega esta migración nueva que, en
-- este orden exacto:
--   1) crea la lista por defecto si no existe,
--   2) reasigna los favoritos con lista_id nulo a esa lista,
--   3) recién ahí declara la columna como NOT NULL.
-- Modificar una migración ya aplicada la dejaría inválida (checksum) y Flyway
-- no dejaría arrancar la aplicación.

-- 1) Lista por defecto
INSERT INTO listas (nombre)
SELECT 'Sin clasificar'
WHERE NOT EXISTS (SELECT 1 FROM listas WHERE nombre = 'Sin clasificar');

-- 2) Reasignar los favoritos huérfanos
UPDATE favoritos
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar')
WHERE lista_id IS NULL;

-- 3) Recién ahora la columna es obligatoria
ALTER TABLE favoritos ALTER COLUMN lista_id SET NOT NULL;
