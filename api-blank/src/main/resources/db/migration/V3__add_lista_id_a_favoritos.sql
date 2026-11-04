-- V3: cada favorito pertenece a una lista (relación N:1).
-- La columna queda nullable de momento: los favoritos creados antes de esta
-- migración no tienen lista. La restricción NOT NULL recién se aplica en V4,
-- una vez que esos filas fueron reasignadas.
ALTER TABLE favoritos ADD COLUMN lista_id BIGINT;

ALTER TABLE favoritos
    ADD CONSTRAINT fk_favoritos_lista
    FOREIGN KEY (lista_id) REFERENCES listas (id);
