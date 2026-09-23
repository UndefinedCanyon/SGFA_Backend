USE sgfa_db;

-- ---------------------------------------------------------
-- Administrador (único, creado directamente por nosotros)
-- ---------------------------------------------------------
INSERT INTO administrador (nombre, correo_electronico, contrasena)
VALUES ('Admin SGFA', 'admin@sgfa.com', 'admin123');

-- ---------------------------------------------------------
-- Lugares
-- ---------------------------------------------------------
INSERT INTO lugar (nombre, direccion) VALUES
('Parque Principal de Fusagasugá', 'Cra 6 # 6-40, Fusagasugá'),
('Plaza de Mercado', 'Cll 8 # 5-20, Fusagasugá');

-- ---------------------------------------------------------
-- Ferias (concepto general, sin fechas)
-- ---------------------------------------------------------
INSERT INTO feria (nombre_feria, id_admin) VALUES
('Feria Artesanal de Fusagasugá', 1),
('Feria Navideña Artesanal', 1);

-- ---------------------------------------------------------
-- Ediciones de feria (fechas y lugar concretos)
-- ---------------------------------------------------------
INSERT INTO edicion_feria (id_feria, id_lugar, fecha_inicio, fecha_fin) VALUES
(1, 1, '2026-10-15', '2026-10-18'),
(1, 2, '2027-03-01', '2027-03-04'),
(2, 1, '2026-12-10', '2026-12-24');

-- ---------------------------------------------------------
-- Artesanos
-- ---------------------------------------------------------
INSERT INTO artesano (nombre, correo_electronico, contrasena, cc, telefono, nombre_emprendimiento, descripcion_corta) VALUES
('Maria Gomez', 'maria@correo.com', 'clave123', '1000111222', '3001112233', 'Tejidos Maria', 'Tejidos artesanales en lana y algodón'),
('Carlos Perez', 'carlos@correo.com', 'clave123', '1000222333', '3002223344', 'Ceramica Carlos', 'Piezas de cerámica hechas a mano'),
('Laura Diaz', 'laura@correo.com', 'clave123', '1000333444', '3003334455', 'Dulces Laura', 'Repostería artesanal y dulces tradicionales');

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
-- Inscripciones (distintos estados para poder probar RF-11/RF-13)
-- ---------------------------------------------------------
INSERT INTO inscripcion (id_artesano, id_edicionferia, fecha_inscripcion, estado) VALUES
(1, 1, '2026-09-01', 'APROBADA'),
(2, 1, '2026-09-02', 'PENDIENTE'),
(3, 1, '2026-09-03', 'RECHAZADA'),
(2, 3, '2026-09-10', 'PENDIENTE');
SELECT * FROM administrador;
SELECT * FROM artesano;
SELECT * FROM inscripcion;