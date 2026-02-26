package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.grupo3.technova.model.Producto;
import com.grupo3.technova.repository.ProductoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // GET /api/productos
    // GET /api/productos?categoria=Auriculares
    @GetMapping("/productos")
    public ResponseEntity<String> listar(@RequestParam(required = false) String categoria) {

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
}
