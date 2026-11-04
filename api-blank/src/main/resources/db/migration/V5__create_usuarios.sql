-- V5: usuarios, dueños de las listas.
-- El password NUNCA se guarda en texto plano: password_hash guarda el hash
-- generado por el PasswordEncoder (BCrypt) de Spring Security.
CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(10)  NOT NULL CHECK (rol IN ('USER', 'ADMIN'))
);
