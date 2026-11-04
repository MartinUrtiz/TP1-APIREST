-- V6: cada lista pasa a pertenecer a un usuario (Usuario -> Lista -> Favorito).
-- Mismo criterio que V4: primero se agrega la columna, luego se resuelven los
-- datos existentes (las listas creadas antes de este punto no tienen dueño)
-- y recién al final se declara NOT NULL. Nunca se edita una migración ya
-- aplicada: esto es una migración nueva (V6).
ALTER TABLE listas ADD COLUMN usuario_id BIGINT;

-- Dueño por defecto para las listas previas (cuenta sin login posible:
-- el password_hash no es un hash válido de BCrypt, nadie puede iniciar sesión)
INSERT INTO usuarios (email, password_hash, rol)
SELECT 'sistema@api-blank.local', 'no-login-posible', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'sistema@api-blank.local');

UPDATE listas
SET usuario_id = (SELECT id FROM usuarios WHERE email = 'sistema@api-blank.local')
WHERE usuario_id IS NULL;

ALTER TABLE listas ALTER COLUMN usuario_id SET NOT NULL;

ALTER TABLE listas
    ADD CONSTRAINT fk_listas_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id);
