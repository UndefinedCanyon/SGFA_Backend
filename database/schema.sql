-- =========================================================
-- SGFA - Software de Gestión para Ferias Artesanales
-- Script de creación de base de datos (DDL)
-- =========================================================

CREATE DATABASE IF NOT EXISTS sgfa_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgfa_db;

-- ---------------------------------------------------------
-- Tabla: administrador
-- ---------------------------------------------------------
CREATE TABLE administrador (
    id_admin        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    correo_electronico VARCHAR(150) NOT NULL,
    contrasena      VARCHAR(255)  NOT NULL,
    CONSTRAINT uq_admin_correo UNIQUE (correo_electronico)
);

-- ---------------------------------------------------------
-- Tabla: artesano
-- ---------------------------------------------------------
CREATE TABLE artesano (
    id_artesano         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    correo_electronico  VARCHAR(150)  NOT NULL,
    contrasena          VARCHAR(255)  NOT NULL,
    cc                  VARCHAR(20)   NOT NULL,
    telefono            VARCHAR(20),
    nombre_emprendimiento VARCHAR(150) NOT NULL,
    descripcion_corta   VARCHAR(300),
    CONSTRAINT uq_artesano_correo UNIQUE (correo_electronico),
    CONSTRAINT uq_artesano_cc UNIQUE (cc)
);

-- ---------------------------------------------------------
-- Tabla: lugar
-- ---------------------------------------------------------
CREATE TABLE lugar (
    id_lugar    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    direccion   VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------
-- Tabla: feria
-- ---------------------------------------------------------
CREATE TABLE feria (
    id_feria     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_feria VARCHAR(150) NOT NULL,
    id_admin     BIGINT NOT NULL,
    CONSTRAINT fk_feria_admin
        FOREIGN KEY (id_admin) REFERENCES administrador(id_admin)
);

-- ---------------------------------------------------------
-- Tabla: edicion_feria
-- ---------------------------------------------------------
CREATE TABLE edicion_feria (
    id_edicionferia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_feria        BIGINT NOT NULL,
    id_lugar        BIGINT NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    CONSTRAINT fk_edicion_feria
        FOREIGN KEY (id_feria) REFERENCES feria(id_feria),
    CONSTRAINT fk_edicion_lugar
        FOREIGN KEY (id_lugar) REFERENCES lugar(id_lugar),
    CONSTRAINT chk_edicion_fechas CHECK (fecha_fin >= fecha_inicio)
);

-- ---------------------------------------------------------
-- Tabla: producto
-- ---------------------------------------------------------
CREATE TABLE producto (
    id_producto  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    cantidad     INT NOT NULL DEFAULT 0,
    id_artesano  BIGINT NOT NULL,
    CONSTRAINT fk_producto_artesano
        FOREIGN KEY (id_artesano) REFERENCES artesano(id_artesano)
);

-- ---------------------------------------------------------
-- Tabla: inscripcion
-- ---------------------------------------------------------
CREATE TABLE inscripcion (
    id_inscripcion   BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_artesano      BIGINT NOT NULL,
    id_edicionferia  BIGINT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_inscripcion_artesano
        FOREIGN KEY (id_artesano) REFERENCES artesano(id_artesano),
    CONSTRAINT fk_inscripcion_edicion
        FOREIGN KEY (id_edicionferia) REFERENCES edicion_feria(id_edicionferia),
    CONSTRAINT chk_inscripcion_estado
        CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT uq_inscripcion_unica
        UNIQUE (id_artesano, id_edicionferia)
);
USE sgfa_db;
SHOW TABLES;