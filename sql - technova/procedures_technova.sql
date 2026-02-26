-- PROCEDIMIENTOS --

USE db_technova;

-- LISTAR PRODUCTOS
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_productos_listar $$
CREATE PROCEDURE sp_productos_listar()
BEGIN
    SELECT id_producto, sku, nombre, descripcion, precio, stock, categoria, imagen
    FROM producto;
END $$
DELIMITER ;

-- LISTAR PRODUCTOS POR CATEGORIA
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_productos_por_categoria $$
CREATE PROCEDURE sp_productos_por_categoria(IN p_categoria VARCHAR(100))
BEGIN
    SELECT id_producto, sku, nombre, descripcion, precio, stock, categoria, imagen
    FROM producto
    WHERE categoria = p_categoria;
END $$
DELIMITER ;

-- LISTAR PEDIDOS POR ESTADO Y FECHA
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_pedidos_listar $$
CREATE PROCEDURE sp_pedidos_listar(IN p_estado VARCHAR(30), IN p_fecha_ini DATE, IN p_fecha_fin DATE)
BEGIN
    SELECT p.id_pedido, p.fecha, p.total_pedido, p.pedido_estado,
           u.id_usuario, u.email, u.rol
    FROM pedido p
    JOIN usuario u ON p.id_usuario = u.id_usuario
    WHERE (p_estado IS NULL OR p.pedido_estado = p_estado)
      AND (p_fecha_ini IS NULL OR DATE(p.fecha) >= p_fecha_ini)
      AND (p_fecha_fin IS NULL OR DATE(p.fecha) <= p_fecha_fin);
END $$
DELIMITER ;

-- LISTAR USUARIOS
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_usuarios_listar $$
CREATE PROCEDURE sp_usuarios_listar()
BEGIN
    SELECT id_usuario, email, rol
    FROM usuario;
END $$
DELIMITER ;

-- LISTAR TODOS LOS PEDIDOS
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_pedidos_listar_todos $$
CREATE PROCEDURE sp_pedidos_listar_todos()
BEGIN
    SELECT p.id_pedido, p.fecha, p.total_pedido, p.pedido_estado,
           u.id_usuario, u.email, u.rol
    FROM pedido p
    JOIN usuario u ON p.id_usuario = u.id_usuario;
END $$
DELIMITER ;

-- LISTAR LINEAS DE PEDIDO
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_lineapedido_listar $$
CREATE PROCEDURE sp_lineapedido_listar()
BEGIN
    SELECT lp.id_linea_pedido, lp.id_pedido, lp.id_producto,
           pr.nombre AS nombre_producto, lp.cantidad, lp.precio_unitario_momento
    FROM linea_pedido lp
    JOIN producto pr ON lp.id_producto = pr.id_producto;
END $$
DELIMITER ;

-- LISTAR MOVIMIENTOS DE INVENTARIO
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_movimientoinventario_listar $$
CREATE PROCEDURE sp_movimientoinventario_listar()
BEGIN
    SELECT mi.id_movimiento, mi.id_producto, pr.nombre AS nombre_producto,
           mi.tipo_movimiento, mi.fecha, mi.cantidad, mi.motivo
    FROM movimiento_inventario mi
    JOIN producto pr ON mi.id_producto = pr.id_producto;
END $$
DELIMITER ;