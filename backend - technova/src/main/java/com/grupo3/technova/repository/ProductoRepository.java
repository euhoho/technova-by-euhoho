package com.grupo3.technova.repository;

import com.grupo3.technova.dto.ProductoRequest;
import com.grupo3.technova.model.Producto;
import com.grupo3.technova.model.enums.EnumCategoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// @Repository le dice a Spring que esta clase es un componente de acceso a datos.
// Spring la registra automáticamente y permite inyectarla en otras clases.
@Repository
public class ProductoRepository {

    // DataSource es el gestor de conexiones a la BD que Spring Boot configura automáticamente con los datos del application.properties.
    private final DataSource dataSource;

    // Spring detecta que necesitamos un DataSource y lo inyecta automáticamente.
    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Devuelve todos los productos llamando al procedimiento almacenado sp_productos_listar()
    public List<Producto> listarProductos() {
        String call = "{CALL sp_productos_listar()}"; 
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call);
             ResultSet rs = cs.executeQuery()) {

            List<Producto> out = new ArrayList<>();
            // rs.next() avanza al siguiente registro y devuelve false cuando no hay más filas
            while (rs.next()) out.add(mapProducto(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error llamando sp_productos_listar", e);
        }
    }

    // Devuelve productos filtrados por categoría llamando a sp_productos_por_categoria()
    public List<Producto> listarPorCategoria(String categoria) {
        String call = "{CALL sp_productos_por_categoria(?)}"; // el ? es el parámetro de entrada
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call)) {

            // Usar setString en vez de concatenar el String evita SQL injection
            cs.setString(1, categoria);
            try (ResultSet rs = cs.executeQuery()) {
                List<Producto> out = new ArrayList<>();
                while (rs.next()) out.add(mapProducto(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error llamando sp_productos_por_categoria", e);
        }
    }

    // Guarda un producto nuevo en la BD. Solo accesible para ADMINISTRADOR.
    // Recibe un ProductoRequest (DTO) en vez de un Producto directamente,porque los datos vienen del cliente y no queremos que el cliente pueda asignar un id_producto (ese lo genera MySQL automáticamente).
    public void guardarProducto(ProductoRequest request) {
        String sql = "INSERT INTO producto (sku, nombre, descripcion, precio, stock, categoria, imagen) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, request.getSku());
            ps.setString(2, request.getNombre());
            ps.setString(3, request.getDescripcion());
            ps.setBigDecimal(4, request.getPrecio());
            ps.setInt(5, request.getStock());
            // .name() convierte el enum a String para guardarlo en la BD
            ps.setString(6, request.getCategoria().name());
            ps.setString(7, request.getImagen());
            ps.executeUpdate();

        } catch (SQLException e) {
            // Error 1062 = SKU duplicado (columna SKU tiene UNIQUE en la BD)
            if (e.getErrorCode() == 1062)
                throw new IllegalArgumentException("Ya existe un producto con ese SKU.");
            throw new RuntimeException("Error guardando producto", e);
        }
    }

    // Método privado auxiliar — convierte una fila del ResultSet en un objeto Producto.
    // Al ser privado solo se puede usar dentro de este repositorio.
    private Producto mapProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getLong("id_producto"),
                rs.getString("sku"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                // EnumCategoria.valueOf() convierte el String de la BD al enum correspondiente
                EnumCategoria.valueOf(rs.getString("categoria")),
                rs.getString("imagen")
        );
    }
}