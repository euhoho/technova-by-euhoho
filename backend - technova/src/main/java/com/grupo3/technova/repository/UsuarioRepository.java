package com.grupo3.technova.repository;

import org.springframework.stereotype.Repository;

import com.grupo3.technova.model.Usuario;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final DataSource dataSource;

    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Para LOGIN (lo que pide el Entregable 3)
    public Optional<Usuario> findByEmailAndPassword(String email, String password) {
        String sql = """
            SELECT id_usuario, email, password, rol
            FROM usuario
            WHERE email = ? AND password = ?
        """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getLong("id_usuario"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("rol")
                    );
                    return Optional.of(u);
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Usuario> findAll() {
        String sql = """
            SELECT id_usuario, email, password, rol
            FROM usuario
        """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getLong("id_usuario"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("rol")
                ));
            }

            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
