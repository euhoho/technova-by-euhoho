package com.grupo3.technova.repository;

import org.springframework.stereotype.Repository;

import com.grupo3.technova.model.Usuario;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // clase especial para representar "puede haber un valor o no

// @Repository le dice a Spring que esta clase es un componente de acceso a datos.
// Spring la registra automáticamente y permite inyectarla en otras clases sin necesidad de hacer "new UsuarioRepository()" manualmente.
@Repository
public class UsuarioRepository {

    // DataSource es el  gestor de conexiones a la DB que Spring Boot configura automáticamente con los datos del "application.properties".
    private final DataSource dataSource;

    // Spring detecta que necesitamos un DataSource y lo inyecta automáticamente.
    // Esto se llama inyección de dependencias — no hacemos "new DataSource()", Spring nos lo da.
    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    // Busca un usuario por email y password. Se usa para el login.
    // Devuelve Optional<Usuario> en vez de Usuario directamente porque el usuario puede no existir.
    public Optional<Usuario> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT id_usuario, email, password, rol FROM usuario WHERE email = ? AND password = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos los dos parámetros ? en orden
            ps.setString(1, email); // primer ?  → email
            ps.setString(2, password); // segundo ? → password  

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Usuario encontrado — lo envolvemos en Optional.of()
                    Usuario u = new Usuario(
                            rs.getLong("id_usuario"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("rol")
                    );
                    return Optional.of(u);
                }
                // Usuario no encontrado — devolvemos Optional.empty()
                // Es mejor que devolver null porque obliga al código que llama a este método a gestionar el caso vacío explícitamente
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Devuelve todos los usuarios. Se usa en GET /api/usuarios.
    public List<Usuario> findAll() {
        String sql = " SELECT id_usuario, email, password, rol FROM usuario";

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
