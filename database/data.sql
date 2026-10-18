SET NAMES utf8mb4;
USE sgfa_db;

-- ---------------------------------------------------------
-- Administrador (contraseña cifrada con BCrypt: admin123)
-- ---------------------------------------------------------
INSERT INTO administrador (nombre, correo_electronico, contrasena)
VALUES ('Admin SGFA', 'admin@sgfa.com', '$2b$10$XCB89QB0Z7p36C/aSdAYceRLKPK7ZexjchofRUwqu0gOvX6Ov.3me');

-- ---------------------------------------------------------
-- Lugares
-- ---------------------------------------------------------
INSERT INTO lugar (nombre, direccion, latitud, longitud) VALUES
('Parque Principal de Fusagasugá', 'Cra 6 # 6-40, Fusagasugá', 4.336900, -74.363600),
('Plaza de Mercado', 'Cll 8 # 5-20, Fusagasugá', 4.339500, -74.360800);

-- ---------------------------------------------------------
-- Ferias
-- ---------------------------------------------------------
INSERT INTO feria (nombre_feria, id_admin) VALUES
('Feria Artesanal de Fusagasugá', 1),
('Feria Navideña Artesanal', 1);

-- ---------------------------------------------------------
-- Ediciones de feria
-- ---------------------------------------------------------
INSERT INTO edicion_feria (id_feria, id_lugar, fecha_inicio, fecha_fin) VALUES
(1, 1, '2026-10-15', '2026-10-18'),
(1, 2, '2027-03-01', '2027-03-04'),
(2, 1, '2026-12-10', '2026-12-24');

-- ---------------------------------------------------------
-- Artesanos (contraseña cifrada con BCrypt: clave123)
-- ---------------------------------------------------------
INSERT INTO artesano (nombre, correo_electronico, contrasena, cc, telefono, nombre_emprendimiento, descripcion_corta) VALUES
('Maria Gomez', 'maria@correo.com', '$2b$10$O5uAiP6go1h1t/57I3lJUuWxFNG3u/D/1utXt6.Uf2kzLmn2pFgy2', '1000111222', '3001112233', 'Tejidos Maria', 'Tejidos artesanales en lana y algodón'),
('Carlos Perez', 'carlos@correo.com', '$2b$10$O5uAiP6go1h1t/57I3lJUuWxFNG3u/D/1utXt6.Uf2kzLmn2pFgy2', '1000222333', '3002223344', 'Ceramica Carlos', 'Piezas de cerámica hechas a mano'),
('Laura Diaz', 'laura@correo.com', '$2b$10$O5uAiP6go1h1t/57I3lJUuWxFNG3u/D/1utXt6.Uf2kzLmn2pFgy2', '1000333444', '3003334455', 'Dulces Laura', 'Repostería artesanal y dulces tradicionales');

-- ---------------------------------------------------------
-- Productos
-- ---------------------------------------------------------
INSERT INTO producto (nombre, precio, cantidad, id_artesano) VALUES
('Ruana de lana', 85000.00, 5, 1),
('Bufanda tejida', 35000.00, 10, 1),
('Vasija de barro', 45000.00, 8, 2),
('Plato decorativo', 30000.00, 12, 2),
('Bocadillo veleño', 5000.00, 50, 3);

-- ---------------------------------------------------------
-- Inscripciones
-- ---------------------------------------------------------
INSERT INTO inscripcion (id_artesano, id_edicionferia, fecha_inscripcion, estado) VALUES
(1, 1, '2026-09-01', 'APROBADA'),
(2, 1, '2026-09-02', 'PENDIENTE'),
(3, 1, '2026-09-03', 'RECHAZADA'),
(2, 3, '2026-09-10', 'PENDIENTE');