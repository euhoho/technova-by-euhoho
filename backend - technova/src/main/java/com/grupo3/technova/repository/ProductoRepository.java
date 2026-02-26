package com.grupo3.technova.repository;

import com.grupo3.technova.model.Producto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductoRepository {

    private final DataSource dataSource;

    public ProductoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Producto> listarProductos() {
        String call = "{CALL sp_productos_listar()}";
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call);
             ResultSet rs = cs.executeQuery()) {

            List<Producto> out = new ArrayList<>();
            while (rs.next()) out.add(mapProducto(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error llamando sp_productos_listar", e);
        }
    }

    public List<Producto> listarPorCategoria(String categoria) {
        String call = "{CALL sp_productos_por_categoria(?)}";
        try (Connection con = dataSource.getConnection();
             CallableStatement cs = con.prepareCall(call)) {

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
