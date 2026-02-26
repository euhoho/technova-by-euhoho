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

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<String> listarUsuarios() {

        List<Usuario> usuarios = repository.findAll();

        JsonArray array = new JsonArray();
        for (Usuario u : usuarios) {
            array.add(u.toJsonObject());
        }

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(array.toString());
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        return repository.findByEmailAndPassword(email, password)
                .map(u -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("status", "ok");
                    json.addProperty("rol", u.getRol());
                    return ResponseEntity
                            .status(200)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(json.toString());
                })
                .orElseGet(() -> {
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
