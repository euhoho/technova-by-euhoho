package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.grupo3.technova.dto.ProductoRequest;
import com.grupo3.technova.model.Producto;
import com.grupo3.technova.model.enums.EnumRol;
import com.grupo3.technova.repository.ProductoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP y que sus métodos devuelven datos directamente, no páginas HTML.
// @RequestMapping("/api") significa que todas las rutas de esta clase empiezan por /api
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoRepository repo;

    // Spring inyecta el repositorio automáticamente
    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // GET /api/productos — accesible para todos, sin restricción de rol
    @GetMapping("/productos")
    public ResponseEntity<String> listar(@RequestParam(required = false) String categoria) {

        // Si no hay categoria llama a listarProductos(), si hay categoria llama a listarPorCategoria(categoria)
        List<Producto> productos = (categoria == null || categoria.isBlank())
                ? repo.listarProductos()
                : repo.listarPorCategoria(categoria);

        JsonArray array = new JsonArray();
        for (Producto p : productos) array.add(p.toJsonObject());

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(array.toString());
    }

    // POST /api/productos — solo accesible para ADMINISTRADOR.
    // El cliente debe enviar la cabecera: user-role: ADMINISTRADOR
    // Si no la envía o el rol no es ADMINISTRADOR, devuelve 403 Forbidden.
    @PostMapping("/productos")
    public ResponseEntity<String> crear(
            // @RequestHeader lee la cabecera "user-role" de la petición HTTP.
            // required = false para que no pete si no viene (lo gestionamos nosotros)
            @RequestHeader(value = "user-role", required = false) String userRole,
            @RequestBody ProductoRequest request) {

        // Comprobamos el rol que viene en la cabecera.
        // Si no viene o no es ADMINISTRADOR cortamos aquí con 403.
        // 403 significa "sé quién eres pero no tienes permiso" (diferente al 401 del login)
        if (userRole == null || !userRole.equals(EnumRol.ADMINISTRADOR.name())) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", "Acceso denegado. Se requiere rol ADMINISTRADOR.");
            return ResponseEntity
                    .status(403)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        }

        try {
            repo.guardarProducto(request);

            // Producto creado correctamente — devolvemos 201 Created
            // 201 es más correcto que 200 cuando se crea un recurso nuevo
            JsonObject json = new JsonObject();
            json.addProperty("status", "ok");
            json.addProperty("message", "Producto creado correctamente.");
            return ResponseEntity
                    .status(201)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());

        } catch (IllegalArgumentException ex) {
            // SKU duplicado u otro error de validación — 400 Bad Request
            JsonObject json = new JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", ex.getMessage());
            return ResponseEntity
                    .status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());

        } catch (Exception ex) {
            // Error inesperado — 500 Internal Server Error
            // No enviamos el mensaje real por seguridad
            JsonObject json = new JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", "Error interno.");
            return ResponseEntity
                    .status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        }
    }
}