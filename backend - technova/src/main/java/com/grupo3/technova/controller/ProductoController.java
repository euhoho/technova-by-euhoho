package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.grupo3.technova.model.Producto;
import com.grupo3.technova.repository.ProductoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP y que sus métodos devuelven datos directamente, no páginas HTML.
// @RequestMapping("/api") significa que todas las rutas de esta clase
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // Atiende GET /api/productos
    // Con el parámetro opcional también atiende GET /api/productos?categoria=PERIFERICOSç
    // @RequestParam(required = false) significa que el parámetro es opcional. Si no viene en la URL, categoria valdrá null
    @GetMapping("/productos")
    public ResponseEntity<String> listar(@RequestParam(required = false) String categoria) {

        //Si no hay categoria llama a listarProductos(), si hay categoria llama a listarPorCategoria(categoria). Equivalente a un if/else
        List<Producto> productos = (categoria == null || categoria.isBlank())
                ? repo.listarProductos()
                : repo.listarPorCategoria(categoria);

        // Construimos el array JSON recorriendo la lista
        // y llamando a toJsonObject() en cada producto        
        JsonArray array = new JsonArray();
        for (Producto p : productos) array.add(p.toJsonObject());

        // ResponseEntity es el objeto que representa la respuesta HTTP completa:
        // status(200) → código HTTP OK
        // contentType(APPLICATION_JSON) → cabecera Content-Type: application/json
        // body(...) → el cuerpo de la respuesta, el JSON como String
        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(array.toString());
    }
}
