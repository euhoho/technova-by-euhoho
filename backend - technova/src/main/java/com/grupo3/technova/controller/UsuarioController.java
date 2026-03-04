package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.grupo3.technova.dto.LoginRequest;
import com.grupo3.technova.model.Usuario;
import com.grupo3.technova.repository.UsuarioRepository;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP y que sus métodos devuelven datos directamente, no páginas HTML.
// @RequestMapping("/api") significa que todas las rutas de esta clase empiezan por /api
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository repository;

    // Spring inyecta el repositorio automáticamente
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

    // POST /api/login — recibe email y password, devuelve ok con datos del usuario o 401
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        // Aquí usamos el Optional que devuelve el repositorio.
        // .map() se ejecuta SI hay usuario (login correcto).
        // .orElseGet() se ejecuta si NO hay usuario (login incorrecto).
        return repository.findByEmailAndPassword(request.getEmail(), request.getPassword())
                .map(u -> {
                    // Login correcto — devolvemos 200 con id, email y rol como pide la guía del entregable 4
                    JsonObject json = new JsonObject();
                    json.addProperty("status", "ok");
                    json.addProperty("id", u.getId_usuario());
                    json.addProperty("nombre", u.getNombre());
                    // .name() convierte el enum a String para el JSON
                    json.addProperty("rol", u.getRol().name());
                    return ResponseEntity
                            .status(200)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(json.toString());
                })
                .orElseGet(() -> {
                    // Credenciales incorrectas — 401 Unauthorized
                    // 401 significa "no sé quién eres" — credenciales incorrectas
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