-- TechNova Store - Base de Datos

DROP DATABASE IF EXISTS db_technova;
CREATE DATABASE db_technova;
USE db_technova;

-- TABLA: usuario
CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol ENUM('Cliente', 'Oficina', 'Administrador') NOT NULL
);

-- TABLA: producto
CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(20) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  descripcion TEXT,
  precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  stock INT NOT NULL CHECK (stock >= 0),
  categoria ENUM('Perifericos','Componentes','Software','Redes') NOT NULL,
  imagen VARCHAR(255)
);

-- TABLA: pedido
CREATE TABLE pedido (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total_pedido DECIMAL(10,2) NOT NULL CHECK (total_pedido >= 0),
  pedido_estado ENUM('CONFIRMADO', 'PREPARADO', 'ENVIADO', 'ENTREGADO') NOT NULL DEFAULT 'CONFIRMADO',
  CONSTRAINT fk_pedido_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- TABLA: linea_pedido
CREATE TABLE linea_pedido (
  id_linea_pedido INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario_momento DECIMAL(10,2) NOT NULL CHECK (precio_unitario_momento >= 0),
  CONSTRAINT fk_linea_pedido_pedido
    FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido)
    ON DELETE CASCADE,
  CONSTRAINT fk_linea_pedido_producto
    FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
);

-- TABLA EXTRA: movimiento_inventario
CREATE TABLE movimiento_inventario (
  id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_producto INT NOT NULL,
  tipo_movimiento ENUM('Entrada','Salida') NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  motivo VARCHAR(255),
  CONSTRAINT fk_movimiento_producto
    FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
);

-- DATOS DE PRUEBA (DML)

-- Usuarios
INSERT INTO usuario (email, password, rol) VALUES
('alvaro@technova.es',  'test123',   'Administrador'),
('ryan@technova.es','test123', 'Oficina'),
('david@technova.es','test123!', 'Cliente');

-- Productos
INSERT INTO producto (sku, nombre, descripcion, precio, stock, categoria, imagen) VALUES

('PER-MG5','Monitor Samsung Odyssey G5','Monitor curvo, 165Hz, 32 pulgadas',199.99,30,'Perifericos','MonitorSamsungOdysseyG5.jpg'),
('PER-TNEWSKILL','Teclado NewSkil Pyro pro','Teclado mecanico, 65%, inalambrico',75.00,100,'Perifericos','TecladoNewSkilPyropro.jpg'),
('PER-RLOGITECH','Logitech G G102 LightSync','Ratón Gaming 8000 DPI',17.90,10,'Perifericos','LogitechGG102LightSync.jpg'),

('COM-MCORSAIR','Memoria Ram Corsair Vengeance','2x16GB DDR5 6000MHz',600.99,9,'Componentes','MemoriaRamCorsairVengeance.jpg'),
('COM-RTX','MSI GeForce RTX 5070 Ti VENTUS','3X OC 16GB GDDR7 Reflex 2 RTX AI DLSS4',1400.90,90,'Componentes','MSIGeForceRTX5070TiVENTUS.jpg'),
('COM-MP600','Corsair MP600 GS 1TB M.2 Gen4','Velocidad lectura secuencial hasta 4800 MB/s',112.22,78,'Componentes','CorsairMP600GS1TBM2Gen4.jpg'),

('SOF-WINDOWS','Windows 11 Pro','Licencia OEM 64 bits Español',200.99,3,'Software','Windows11Pro.jpg'),
('SOF-KASPERSKY','Kaspersky Pro','Licencia AntiVirus Kaspersky 1 año',29.95,3,'Software','KasperskyPro.jpg'),
('SOF-OFFICE','Microsoft Office Professional Plus','Descarga digital Microsoft Office Professional Plus ',149.99,3,'Software','MicrosoftOfficeProfessionalPlus.jpg'),

('RED-DLINK','Módem D-Link F518/M 5G','Wi-Fi 6 dual band hasta 1800 Mbps',295.87,3,'Redes','ModemDLinkF518M5G.jpg'),
('RED-USB','TP-Link UB5A','Adaptador Nano USB Bluetooth 5.0',9.99,1,'Redes','TPLinkUB5A.jpg'),
('RED-TARJETA','ASUS PCE-AXE5400','Tarjeta de Red WiFi AXE5400 con Bluetooth',32.95,4,'Redes','ASUSPCEAXE5400.jpg'),

