-- BASE DE DATOS --

DROP DATABASE IF EXISTS db_technova;
CREATE DATABASE db_technova;
USE db_technova;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
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
    categoria ENUM('COMPONENTES', 'PERIFERICOS', 'REDES', 'SOFTWARE') NOT NULL,
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

INSERT INTO usuario (email, password, rol) VALUES
('anagarcia@technova.es', '12345a!!FDDV', 'OFICINA'),
('saragonzalez@technova.es', 'asdjfj21!CC', 'OFICINA'),
('alvaromartin@technova.es', 'vfjjdll?Vj1', 'ADMINISTRADOR'),
('shaghyasghari@technova.es', 'bccvcc98!D7', 'ADMINISTRADOR'),
('davidfraile@technova.es', 'mdkdjsjk3?R742', 'ADMINISTRADOR'),
('rayantorres@technova.es', 'lfjgjbjd1!F3', 'ADMINISTRADOR'),
('javiervs@gmail.com', 'uyuyuyuy124.S', 'CLIENTE'),
('lorenzop@gmail.com', 'iuinkuhn987!A', 'CLIENTE'),
('danie23@gmail.com', 'lnhhbhhA!34', 'CLIENTE'),
('antoniosf@gmail.com', 'lnhsdkAhA!34', 'CLIENTE'),
('maria837a@gmail.com', 'mcsjfwDS!55', 'CLIENTE'),
('anitaflores69@gmail.com', 'dvjjdje%D2', 'CLIENTE');

INSERT INTO producto (sku, nombre, descripcion, precio, stock, categoria, imagen) VALUES
('PER-MG5', 'Monitor Samsung Odyssey G5', 'Monitor curvo, 165Hz, 32 pulgadas', 199.99, 30, 'PERIFERICOS', 'MonitorSamsungOdysseyG5.jpg'),
('PER-TNEWSKILL', 'Teclado NewSkil Pyro pro', 'Teclado mecanico, 65%, inalambrico', 75.00, 100, 'PERIFERICOS', 'TecladoNewSkilPyropro.jpg'),
('PER-RLOGITECH', 'Logitech G G102 LightSync', 'Raton Gaming 8000 DPI', 17.90, 10, 'PERIFERICOS', 'LogitechGG102LightSync.jpg'),
('COM-MCORSAIR', 'Memoria Ram Corsair Vengeance', '2x16GB DDR5 6000MHz', 600.99, 9, 'COMPONENTES', 'MemoriaRamCorsairVengeance.jpg'),
('COM-RTX', 'MSI GeForce RTX 5070 Ti VENTUS', '3X OC 16GB GDDR7 Reflex 2 RTX AI DLSS4', 1400.90, 90, 'COMPONENTES', 'MSIGeForceRTX5070TiVENTUS.jpg'),
('COM-MP600', 'Corsair MP600 GS 1TB M.2 Gen4', 'Velocidad lectura secuencial hasta 4800 MB/s', 112.22, 78, 'COMPONENTES', 'CorsairMP600GS1TBM2Gen4.jpg'),
('SOF-WINDOWS', 'Windows 11 Pro', 'Licencia OEM 64 bits Espanol', 200.99, 3, 'SOFTWARE', 'Windows11Pro.jpg'),
('SOF-KASPERSKY', 'Kaspersky Pro', 'Licencia AntiVirus Kaspersky 1 anio', 29.95, 3, 'SOFTWARE', 'KasperskyPro.jpg'),
('SOF-OFFICE', 'Microsoft Office Professional Plus', 'Descarga digital Microsoft Office Professional Plus', 149.99, 3, 'SOFTWARE', 'MicrosoftOfficeProfessionalPlus.jpg'),
('RED-DLINK', 'Modem D-Link F518/M 5G', 'Wi-Fi 6 dual band hasta 1800 Mbps', 295.87, 3, 'REDES', 'ModemDLinkF518M5G.jpg'),
('RED-USB', 'TP-Link UB5A', 'Adaptador Nano USB Bluetooth 5.0', 9.99, 1, 'REDES', 'TPLinkUB5A.jpg'),
('RED-TARJETA', 'ASUS PCE-AXE5400', 'Tarjeta de Red WiFi AXE5400 con Bluetooth', 32.95, 4, 'REDES', 'ASUSPCEAXE5400.jpg');

-- totales calculados desde las lineas:
-- pedido 1: 17.90 + 75.00 + 199.99 = 292.89
-- pedido 2: 295.87 + 75.00 + 17.90 = 388.77
-- pedido 3: 295.87
-- pedido 4: 17.90*2 + 75.00 = 110.80

INSERT INTO pedido (id_usuario, total_pedido, pedido_estado) VALUES
(7, 292.89, 'ENVIADO'),
(8, 388.77, 'CONFIRMADO'),
(9, 295.87, 'ENTREGADO'),
(11, 110.80, 'PREPARADO');

INSERT INTO linea_pedido (id_pedido, id_producto, cantidad, precio_unitario_momento) VALUES
-- Pedido 1 - javiervs@gmail.com
(1, 3, 1, 17.90),
(1, 2, 1, 75.00),
(1, 1, 1, 199.99),
-- Pedido 2 - lorenzop@gmail.com
(2, 10, 1, 295.87),
(2, 2, 1, 75.00),
(2, 3, 1, 17.90),
-- Pedido 3 - danie23@gmail.com
(3, 10, 1, 295.87),
-- Pedido 4 - maria837a@gmail.com
(4, 3, 2, 17.90),
(4, 2, 1, 75.00);

INSERT INTO movimiento_inventario (id_producto, tipo_movimiento, cantidad, motivo) VALUES
(3, 'ENTRADA', 20, 'ALTA DE STOCK'),
(2, 'SALIDA', 1, 'ARTICULO DEFECTUOSO');