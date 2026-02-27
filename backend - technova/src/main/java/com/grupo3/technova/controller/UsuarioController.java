package com.grupo3.technova.controller;
    
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.grupo3.technova.model.Usuario;
import com.grupo3.technova.repository.UsuarioRepository;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP y que sus métodos devuelven datos directamente, no páginas HTML.
// @RequestMapping("/api") significa que todas las rutas de esta clase
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    // GET /api/usuarios — devuelve todos los usuarios sin password
    @GetMapping("/usuarios")
    public ResponseEntity<String> listarUsuarios() {

        List<Usuario> usuarios = repository.findAll();

        JsonArray array = new JsonArray();
        for (Usuario u : usuarios) {
            array.add(u.toJsonObject()); // toJsonObject() ya excluye el password
        }

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(array.toString());
    }

    // POST /api/login — recibe email y password, devuelve ok o error 401
    // @RequestBody hace que Spring lea el JSON del cuerpo de la petición y lo convierta automáticamente en un Map<String, String>.
    // Ejemplo de JSON que llega: { "email": "x@x.com", "password": "1234" }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        // Aquí usamos el Optional que devuelve el repositorio.
        // .map() se ejecuta SI hay usuario (login correcto).
        // .orElseGet() se ejecuta si NO hay usuario (login incorrecto).
        // Es como un if/else pero encadenado sobre el Optional.
        return repository.findByEmailAndPassword(email, password)
                .map(u -> {
                    // Usuario encontrado — devolvemos 200 con status ok y el rol
                    JsonObject json = new JsonObject();
                    json.addProperty("status", "ok");
                    json.addProperty("rol", u.getRol());
                    return ResponseEntity
                            .status(200)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(json.toString());
                })
                .orElseGet(() -> {
                    // Usuario no encontrado — devolvemos 401 Unauthorized
                    // 401 es el código HTTP estándar para credenciales incorrectas
                    JsonObject json = new JsonObject();
                    json.addProperty("status", "error");
                    json.addProperty("message", "Credenciales incorrectas");
                    return ResponseEntity
                            .status(401)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(json.toString());
                });
    }
}