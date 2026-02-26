package com.grupo3.technova.repository;

import com.grupo3.technova.model.Pedido;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PedidoRepository {

    private final DataSource dataSource;

    public PedidoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Pedido> listarPedidos(String estado, Date fechaIni, Date fechaFin) {
        String call = "{CALL sp_pedidos_listar(?, ?, ?)}";

        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call)) {

            // estado (puede ser null)
            if (estado == null || estado.isBlank()) cs.setNull(1, Types.VARCHAR);
            else cs.setString(1, estado);

            // fechas (pueden ser null)
            if (fechaIni == null) cs.setNull(2, Types.DATE);
            else cs.setDate(2, fechaIni);

            if (fechaFin == null) cs.setNull(3, Types.DATE);
            else cs.setDate(3, fechaFin);

            try (ResultSet rs = cs.executeQuery()) {
                List<Pedido> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Pedido(
                            rs.getLong("id_pedido"),
                            rs.getTimestamp("fecha"),
                            rs.getBigDecimal("total_pedido"),
                            rs.getString("pedido_estado"),
                            rs.getLong("id_usuario"),
                            rs.getString("email"),
                            rs.getString("rol")
                    ));
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error llamando sp_pedidos_listar", e);
        }
    }

    public long crearPedidoYDescontarStock(long idUsuario, List<java.util.Map<String, Object>> items) {

        String sqlInsertPedido = "INSERT INTO pedido (id_usuario, total_pedido) VALUES (?, 0)";
        String sqlSelectProdForUpdate = "SELECT precio, stock FROM producto WHERE id_producto = ? FOR UPDATE";
        String sqlInsertLinea = "INSERT INTO linea_pedido (id_pedido, id_producto, cantidad, precio_unitario_momento) VALUES (?, ?, ?, ?)   ";
        String sqlUpdateStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
        String sqlTotal = "SELECT IFNULL(SUM(cantidad * precio_unitario_momento), 0) AS total FROM linea_pedido WHERE id_pedido = ?";
        String sqlUpdateTotal = "UPDATE pedido SET total_pedido = ? WHERE id_pedido = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                // 1) Insertar pedido y recuperar id generado
                long idPedido;
                try (PreparedStatement ps = con.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se pudo recuperar id_pedido");
                        idPedido = keys.getLong(1);
                    }
                }

                // 2) Por cada item: leer precio/stock, insertar línea y restar stock
                for (java.util.Map<String, Object> item : items) {
                    Number idProdN = (Number) item.get("id_producto");
                    Number cantN = (Number) item.get("cantidad");

                    if (idProdN == null) throw new IllegalArgumentException("Falta id_producto en un item");
                    if (cantN == null) throw new IllegalArgumentException("Falta cantidad en un item");

                    long idProducto = idProdN.longValue();
                    int cantidad = cantN.intValue();

                    if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida para id_producto=" + idProducto);

                    // 2.1) Leer y bloquear producto (evita carreras)
                    java.math.BigDecimal precio;
                    int stock;
                    try (PreparedStatement ps = con.prepareStatement(sqlSelectProdForUpdate)) {
                        ps.setLong(1, idProducto);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new IllegalArgumentException("Producto no existe: id_producto=" + idProducto);
                            precio = rs.getBigDecimal("precio");
                            stock = rs.getInt("stock");
                        }
                    }

                    if (stock < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente para id_producto=" + idProducto);
                    }

                    // 2.2) Insertar línea con el precio del momento
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertLinea)) {
                        ps.setLong(1, idPedido);
                        ps.setLong(2, idProducto);
                        ps.setInt(3, cantidad);
                        ps.setBigDecimal(4, precio);
                        ps.executeUpdate();
                    }

                    // 2.3) Descontar stock (extra de nota)
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateStock)) {
                        ps.setInt(1, cantidad);
                        ps.setLong(2, idProducto);
                        ps.setInt(3, cantidad);
                        int updated = ps.executeUpdate();
                        if (updated == 0) throw new IllegalArgumentException("No se pudo descontar stock para id_producto=" + idProducto);
                    }
                }

                // 3) Calcular total y actualizar pedido
                java.math.BigDecimal total;
                try (PreparedStatement ps = con.prepareStatement(sqlTotal)) {
                    ps.setLong(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getBigDecimal("total");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlUpdateTotal)) {
                    ps.setBigDecimal(1, total);
                    ps.setLong(2, idPedido);
                    ps.executeUpdate();
                }

                con.commit();
                return idPedido;

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
        // MySQL: 1452 = FK falla (usuario o producto no existe)
        if (e.getErrorCode() == 1452) {
            throw new IllegalArgumentException("Usuario o producto no existe (FK).");
        }
        // MySQL: 1062 = duplicado (por si alguna vez pasa)
        if (e.getErrorCode() == 1062) {
            throw new IllegalArgumentException("Datos duplicados.");
        }
        throw new RuntimeException("Error creando pedido", e);
    }
    }
}
