package com.grupo3.technova.repository;

import com.grupo3.technova.model.Producto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// @Repository le dice a Spring que esta clase es un componente de acceso a datos.
// Spring la registra automáticamente y permite inyectarla en otras clases sin necesidad de hacer "new ProductoRepository()" manualmente.
@Repository
public class ProductoRepository {

    // DataSource es el  gestor de conexiones a la DB que Spring Boot configura automáticamente con los datos del "application.properties".
    private final DataSource dataSource;

    // Spring detecta que necesitamos un DataSource y lo inyecta automáticamente.
    // Esto se llama inyección de dependencias — no hacemos "new DataSource()", Spring nos lo da.
    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Devuelve todos los productos llamando al procedimiento almacenado sp_productos_listar()
    public List<Producto> listarProductos() {
        String call = "{CALL sp_productos_listar()}"; // sintaxis JDBC para llamar a un stored procedure
        // try-with-resources — abre la conexión, el statement y el ResultSet y los cierra automáticamente al acabar aunque haya un error.
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call);
             ResultSet rs = cs.executeQuery()) {

            List<Producto> out = new ArrayList<>();
            // rs.next() avanza al siguiente registro y devuelve false cuando no hay más filas
            while (rs.next()) out.add(mapProducto(rs));
            return out;

        } catch (SQLException e) {
            // Convertimos SQLException en RuntimeException (para no tener que declarar "throws SQLException" en todos los métodos
            throw new RuntimeException("Error llamando sp_productos_listar", e);
        }
    }

    // Devuelve productos filtrados por categoría llamando a sp_productos_por_categoria()
    public List<Producto> listarPorCategoria(String categoria) {
        String call = "{CALL sp_productos_por_categoria(?)}"; // el ? es el parámetro de entrada
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call)) {

            // Asignamos el valor al parámetro ? en la posición 1 (el primero y único)
            // Usar setString en vez de concatenar el String evita SQL injection
            cs.setString(1, categoria);
            // El ResultSet se abre aquí dentro porque el CallableStatement necesita tener los parámetros asignados antes de ejecutarse
            try (ResultSet rs = cs.executeQuery()) {
                List<Producto> out = new ArrayList<>();
                while (rs.next()) out.add(mapProducto(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error llamando sp_productos_por_categoria", e);
        }
    }

    // Mapeamos — convierte una fila del ResultSet en un objeto Producto. Para devolver en JSON.
    private Producto mapProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getLong("id_producto"),
                rs.getString("sku"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getString("categoria"),
                rs.getString("imagen")
        );
    }
}
