package com.grupo3.technova.repository;

import com.grupo3.technova.model.Pedido;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PedidoRepository {

   // DataSource es el  gestor de conexiones a la DB que Spring Boot configura automáticamente con los datos del "application.properties".
    private final DataSource dataSource;

    // Spring detecta que necesitamos un DataSource y lo inyecta automáticamente.
    // Esto se llama inyección de dependencias — no hacemos "new DataSource()", Spring nos lo da.
    public PedidoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Lista pedidos filtrando por estado y/o fechas.
    public List<Pedido> listarPedidos(String estado, Date fechaIni, Date fechaFin) {
        String call = "{CALL sp_pedidos_listar(?, ?, ?)}";

        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call)) {

            // Para cada parámetro comprobamos si viene vacío o null.
            if (estado == null || estado.isBlank()) cs.setNull(1, Types.VARCHAR);
            else cs.setString(1, estado);

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
                            rs.getBigDecimal("total_pedido"), // Timestamp porque la columna es DATETIME
                            rs.getString("pedido_estado"),
                            // estos tres vienen del JOIN con usuario que tiene el procedimiento almacenado
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

        // Definimos todas las SQLs al principio para tenerlas localizadas y legibles.
        // Ninguna concatena Strings — todas usan ? para evitar SQL injection.
        String sqlInsertPedido = "INSERT INTO pedido (id_usuario, total_pedido) VALUES (?, 0)";
        String sqlSelectProdForUpdate = "SELECT precio, stock FROM producto WHERE id_producto = ? FOR UPDATE"; // bloquea la fila del producto para evitar que otro pedido pueda leer o modificar el stock hasta que terminemos esta transacción
        String sqlInsertLinea = "INSERT INTO linea_pedido (id_pedido, id_producto, cantidad, precio_unitario_momento) VALUES (?, ?, ?, ?)   ";
        String sqlUpdateStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?"; // el AND stock >= ? es una medida extra de seguridad para evitar que el stock baje de 0 aunque haya un error en la lógica (por ejemplo, si dos pedidos intentan comprar el mismo producto con poco stock al mismo tiempo, solo uno podrá descontar el stock gracias a esta condición y al bloqueo FOR UPDATE)
        String sqlTotal = "SELECT IFNULL(SUM(cantidad * precio_unitario_momento), 0) AS total FROM linea_pedido WHERE id_pedido = ?"; //IFNULL(..., 0) hace que devuelva 0 en vez de null si no hay líneas
        String sqlUpdateTotal = "UPDATE pedido SET total_pedido = ? WHERE id_pedido = ?";

        try (Connection con = dataSource.getConnection()) {
            // Desactivamos el autocommit — por defecto cada SQL se confirma sola.
            // Al desactivarlo, nada se guarda en la DB hasta que llamemos a commit().
            // Si algo falla a mitad podemos llamar a rollback() y deshacer todo.
            con.setAutoCommit(false);

            try {
                // PASO 1: Insertar la cabecera del pedido
                long idPedido;
                try (PreparedStatement ps = con.prepareStatement(sqlInsertPedido, Statement.RETURN_GENERATED_KEYS)) {
                    // RETURN_GENERATED_KEYS le dice a JDBC que queremos recuperar el id_pedido que MySQL generó automáticamente con AUTO_INCREMENT
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate(); // ejecuta el INSERT
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se pudo recuperar id_pedido");
                        idPedido = keys.getLong(1); // guardamos el id para usarlo en las líneas
                    }
                }

                // PASO 2: Procesar cada producto del carrito
                for (java.util.Map<String, Object> item : items) {
                    // items es la lista de productos del carrito.
                    // Cada item es un Map con "id_producto" y "cantidad".
                    // Recorremos uno a uno para procesar cada producto.

                    // Los valores vienen como Object porque el JSON no distingue entre Integer y Long. Casteamos a Number y luego convertimos al tipo que necesitamos.
                    Number idProdN = (Number) item.get("id_producto");
                    Number cantN = (Number) item.get("cantidad");

                    // Validaciones — si falta algún campo lanzamos error con mensaje claro
                    if (idProdN == null) throw new IllegalArgumentException("Falta id_producto en un item");
                    if (cantN == null) throw new IllegalArgumentException("Falta cantidad en un item");

                    long idProducto = idProdN.longValue();
                    int cantidad = cantN.intValue();

                    if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida para id_producto=" + idProducto);

                    // PASO 2.1: Leer precio y stock, bloqueando la fila
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

                    // Comprobamos antes de insertar nada.
                    // Si no hay suficiente stock lanzamos error y el rollback deshará todo lo que hayamos hecho hasta aquí.
                    if (stock < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente para id_producto=" + idProducto);
                    }

                    // PASO 2.2: Insertar línea de pedido
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertLinea)) {
                        ps.setLong(1, idPedido);
                        ps.setLong(2, idProducto);
                        ps.setInt(3, cantidad);
                        ps.setBigDecimal(4, precio);
                        // Guardamos el precio del momento — si el producto sube de precio mañana, el pedido de hoy conserva el precio que tenía al comprarlo
                        ps.executeUpdate();
                    }

                    // PASO 2.3: Descontar stock
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdateStock)) {
                        ps.setInt(1, cantidad);
                        ps.setLong(2, idProducto);
                        ps.setInt(3, cantidad); // tercer ? es para el AND stock >= ?
                        int updated = ps.executeUpdate();
                        if (updated == 0) throw new IllegalArgumentException("No se pudo descontar stock para id_producto=" + idProducto);
                        // Si updated es 0 significa que entre el SELECT de arriba y este UPDATE otro usuario compró el último stock. El AND stock >= ? lo detecta y no actualiza ninguna fila — lanzamos error en vez de dejar stock negativo.
                    }
                }

                // PASO 3: Calcular total y actualizar pedido
                java.math.BigDecimal total;
                try (PreparedStatement ps = con.prepareStatement(sqlTotal)) {
                    ps.setLong(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        total = rs.getBigDecimal("total");
                        // Calculamos el total desde la DB sumando las líneas en vez de hacerlo en Java para garantizar consistencia
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlUpdateTotal)) {
                    ps.setBigDecimal(1, total);
                    ps.setLong(2, idPedido);
                    ps.executeUpdate(); // actualizamos el pedido con el total real
                }

                con.commit(); // En este momento todo se guarda de forma permanente en la BD.
                return idPedido; // devolvemos el id para que el controller lo incluya en la respuesta

            } catch (Exception ex) {
                con.rollback(); // si algo falla, deshace los cambios
                throw ex; // relanzamos el error para que el controller lo gestione
            } finally {
                con.setAutoCommit(true);
                // finally siempre se ejecuta, haya error o no.
                // Restauramos el autocommit
            }

        } catch (SQLException e) {
        // Errores específicos de MySQL identificados por su código numérico
        if (e.getErrorCode() == 1452) {
            throw new IllegalArgumentException("Usuario o producto no existe (FK).");
        }
        if (e.getErrorCode() == 1062) {
            throw new IllegalArgumentException("Datos duplicados.");
        }
        throw new RuntimeException("Error creando pedido", e);
    }
    }
}
