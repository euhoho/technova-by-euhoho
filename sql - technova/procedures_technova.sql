-- TechNova Store - Procedimientos de LECTURA

DELIMITER $$

-- Procedimiento: listar todos los productos
DROP PROCEDURE IF EXISTS sp_productos_listar$$
CREATE PROCEDURE sp_productos_listar()
BEGIN
  SELECT
    id_producto,
    sku,
    nombre,
    descripcion,
    precio,
    stock,
    categoria,
    imagen
  FROM producto
  ORDER BY id_producto;
END$$

-- Procedimiento: listar productos por categoría
DROP PROCEDURE IF EXISTS sp_productos_por_categoria$$
CREATE PROCEDURE sp_productos_por_categoria(IN p_categoria VARCHAR(100))
BEGIN
  SELECT
    id_producto,
    sku,
    nombre,
    descripcion,
    precio,
    stock,
    categoria,
    imagen
  FROM producto
  WHERE categoria = p_categoria
  ORDER BY id_producto;
END$$

-- Procedimiento: listar pedidos con filtros opcionales
DROP PROCEDURE IF EXISTS sp_pedidos_listar$$
CREATE PROCEDURE sp_pedidos_listar(
  IN p_estado VARCHAR(30),
  IN p_fecha_ini DATE,
  IN p_fecha_fin DATE
)
BEGIN
  SELECT
    p.id_pedido,
    p.fecha,
    p.total_pedido,
    p.pedido_estado,
    u.id_usuario,
    u.email,
    u.rol
  FROM pedido p
  INNER JOIN usuario u ON u.id_usuario = p.id_usuario
  WHERE
    (p_estado IS NULL OR p_estado = '' OR p.pedido_estado = p_estado)
    AND (p_fecha_ini IS NULL OR DATE(p.fecha) >= p_fecha_ini)
    AND (p_fecha_fin IS NULL OR DATE(p.fecha) <= p_fecha_fin)
  ORDER BY p.fecha DESC, p.id_pedido DESC;
END$$

DELIMITER ;
