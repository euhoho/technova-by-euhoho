-- BASE DE DATOS --

DROP DATABASE IF EXISTS db_technova;
CREATE DATABASE db_technova;
USE db_technova;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('CLIENTE', 'OFICINA', 'ADMINISTRADOR') NOT NULL
);

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    categoria ENUM('AURICULARES', 'PANTALLAS', 'RELOJES', 'MOVILES') NOT NULL,
    imagen VARCHAR(255)
);

CREATE TABLE pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_pedido DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pedido_estado ENUM('CONFIRMADO', 'PREPARADO', 'ENVIADO', 'ENTREGADO'),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE linea_pedido (
    id_linea_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario_momento DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

CREATE TABLE movimiento_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    tipo_movimiento ENUM('ENTRADA', 'SALIDA') NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    motivo VARCHAR(255),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- INSERTS --
INSERT INTO usuario (email, nombre, password, rol) VALUES
('anagarcia@technova.es',       'Ana García',          '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'OFICINA'),
('saragonzalez@technova.es',    'Sara González',       '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'OFICINA'),
('alvaromartin@technova.es',    'Álvaro Martín',       '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'ADMINISTRADOR'),
('shaghyasghari@technova.es',   'Shagh Yasghari',      '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'ADMINISTRADOR'),
('davidfraile@technova.es',     'David Fraile',        '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'ADMINISTRADOR'),
('rayantorres@technova.es',     'Rayan Torres',        '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'ADMINISTRADOR'),
('javiervs@gmail.com',          'Javier Villanueva',   '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE'),
('lorenzop@gmail.com',          'Lorenzo Pérez',       '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE'),
('danie23@gmail.com',           'Daniel Sánchez',      '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE'),
('antoniosf@gmail.com',         'Antonio Fernández',   '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE'),
('maria837a@gmail.com',         'María Rodríguez',     '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE'),
('anitaflores69@gmail.com',     'Anita Flores',        '$2a$10$WKf1IWsd//57vdE2Uc3Meu1jzECAtgdy2lkX54ZRSltvrgyaM5oR2', 'CLIENTE');

INSERT INTO producto (sku, nombre, descripcion, precio, stock, categoria, imagen) VALUES
('PAN-EDXG',   'Display XG',          'Mejora tu espacio de trabajo con Display y lleva tu productividad a otro nivel.',         649.99, 20, 'PANTALLAS',   'DisplayXG.png'),
('PAN-EDXE',   'Display XE',          'Mejora tu espacio de trabajo con Display y lleva tu productividad a otro nivel.',            649.99, 15, 'PANTALLAS',   'DisplayXE.png'),
('AUR-OR27N',  'Headphones or-27n',    'Ya sea que estés en movimiento o entrenando, estos auriculares están diseñados para seguir tu ritmo sin esfuerzo.',   97.99, 50, 'AURICULARES', 'HeadphonesOR27N.png'),
('AUR-W96C',   'Headphones w-96c',     'Ya sea que estés en movimiento o entrenando, estos auriculares están diseñados para seguir tu ritmo sin esfuerzo.',              129.99, 40, 'AURICULARES', 'HeadphonesW96C.png'),
('AUR-Z23C',   'Headphones z-23c',     'Ya sea que estés en movimiento o entrenando, estos auriculares están diseñados para seguir tu ritmo sin esfuerzo.',            149.99, 35, 'AURICULARES', 'HeadphonesZ23C.png'),
('MOV-P15B',   'Phone 15 Black',       'Experimenta la cima de la tecnología y el diseño con Phone. Funciones integradas, pantalla impresionante y potente rendimiento redefinen lo que es posible en el mundo de los smartphones.',              799.99, 25, 'MOVILES',     'Phone15Black.png'),
('MOV-P15R',   'Phone 15 Red',         'Experimenta la cima de la tecnología y el diseño con Phone. Funciones integradas, pantalla impresionante y potente rendimiento redefinen lo que es posible en el mundo de los smartphones.',              799.99, 25, 'MOVILES',     'Phone15Red.png'),
('REL-SF3B',   'Watch SF 3 Black',     'Mantente conectado, organizado y motivado con Watch. Conectividad total, seguimiento fitness, diseño elegante y funciones inteligentes, todo en un solo dispositivo.',    319.99, 30, 'RELOJES',     'WatchSF3Black.png'),
('REL-SF4O',   'Watch SF 4 Orange',    'Mantente conectado, organizado y motivado con Watch. Conectividad total, seguimiento fitness, diseño elegante y funciones inteligentes, todo en un solo dispositivo.', 349.99, 20, 'RELOJES',     'WatchSF4Orange.png');

-- pedido 1: 97.99 + 129.99 + 799.99 = 1027.97
-- pedido 2: 649.99 + 319.99 = 969.98
-- pedido 3: 799.99
-- pedido 4: 97.99*2 + 349.99 = 545.97

INSERT INTO pedido (id_usuario, total_pedido, pedido_estado) VALUES
(7,  1027.97, 'ENVIADO'),
(8,   969.98, 'CONFIRMADO'),
(9,   799.99, 'ENTREGADO'),
(11,  545.97, 'PREPARADO');

INSERT INTO linea_pedido (id_pedido, id_producto, cantidad, precio_unitario_momento) VALUES
-- Pedido 1 - javiervs@gmail.com
(1, 3, 1,  97.99),
(1, 4, 1, 129.99),
(1, 6, 1, 799.99),
-- Pedido 2 - lorenzop@gmail.com
(2, 1, 1, 649.99),
(2, 8, 1, 319.99),
-- Pedido 3 - danie23@gmail.com
(3, 7, 1, 799.99),
-- Pedido 4 - maria837a@gmail.com
(4, 3, 2,  97.99),
(4, 9, 1, 349.99);

INSERT INTO movimiento_inventario (id_producto, tipo_movimiento, cantidad, motivo) VALUES
(3, 'ENTRADA', 20, 'ALTA DE STOCK'),
(4, 'SALIDA',   1, 'ARTICULO DEFECTUOSO');