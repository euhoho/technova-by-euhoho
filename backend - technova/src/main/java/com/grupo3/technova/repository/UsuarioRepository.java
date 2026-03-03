package com.grupo3.technova.repository;

import com.grupo3.technova.model.Usuario;
import com.grupo3.technova.model.enums.EnumRol;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

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

    // BCryptPasswordEncoder es la clase que hashea y compara contraseñas.
    // Lo instanciamos una sola vez y lo reutilizamos en todos los métodos.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Spring detecta que necesitamos un DataSource y lo inyecta automáticamente.
    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Busca un usuario por email y compara la password con BCrypt.
    // Ya no buscamos por password en la BD — primero buscamos por email, luego comparamos la password enviada con el hash guardado en Java.
    public Optional<Usuario> findByEmailAndPassword(String email, String password) {

        String sql = "SELECT id_usuario, email, password, rol FROM usuario WHERE email = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("password");

                    // encoder.matches() compara la password en texto plano con el hash de la BD.
                    // BCrypt genera un hash distinto cada vez, pero matches() sabe compararlos.
                    // Si la password no coincide con el hash, devolvemos Optional.empty().
                    if (!encoder.matches(password, hashGuardado)) {
                        return Optional.empty();
                    }

                    Usuario u = new Usuario(
                            rs.getLong("id_usuario"),
                            rs.getString("email"),
                            rs.getString("password"),
                            // EnumRol.valueOf() convierte el String de la BD al enum correspondiente
                            EnumRol.valueOf(rs.getString("rol"))
                    );
                    return Optional.of(u);
                }
                // Usuario no encontrado — devolvemos Optional.empty()
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Devuelve todos los usuarios. Se usa en GET /api/usuarios.
    public List<Usuario> findAll() {
        String sql = "SELECT id_usuario, email, password, rol FROM usuario";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getLong("id_usuario"),
                        rs.getString("email"),
                        rs.getString("password"),
                        // EnumRol.valueOf() convierte el String de la BD al enum correspondiente
                        EnumRol.valueOf(rs.getString("rol"))
                ));
            }
            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}