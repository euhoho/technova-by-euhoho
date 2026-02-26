package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.grupo3.technova.model.Pedido;
import com.grupo3.technova.repository.PedidoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PedidoController {

    private final PedidoRepository repo;

    public PedidoController(PedidoRepository repo) {
        this.repo = repo;
    }

    // GET /api/pedidos
    // GET /api/pedidos?estado=CONFIRMADO&fechaIni=2025-01-01&fechaFin=2025-12-31
    @GetMapping("/pedidos")
    public ResponseEntity<String> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaIni,
            @RequestParam(required = false) String fechaFin
    ) {
        Date fIni = (fechaIni == null || fechaIni.isBlank()) ? null : Date.valueOf(fechaIni);
        Date fFin = (fechaFin == null || fechaFin.isBlank()) ? null : Date.valueOf(fechaFin);

        List<Pedido> pedidos = repo.listarPedidos(estado, fIni, fFin);

        JsonArray array = new JsonArray();
        for (Pedido p : pedidos) array.add(p.toJsonObject());

        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(array.toString());
    }
    @PostMapping("/pedidos")
    public ResponseEntity<String> crear(@RequestBody java.util.Map<String, Object> body) {
        try {
            // Espera JSON:
            // { "id_usuario": 1, "items": [ { "id_producto": 21, "cantidad": 2 }, ... ] }

            Number idUsuarioN = (Number) body.get("id_usuario");
            if (idUsuarioN == null) throw new IllegalArgumentException("Falta id_usuario");

            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> items =
                    (java.util.List<java.util.Map<String, Object>>) body.get("items");

            if (items == null || items.isEmpty()) throw new IllegalArgumentException("El pedido debe tener items");

            long idPedido = repo.crearPedidoYDescontarStock(idUsuarioN.longValue(), items);

            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "ok");
            json.addProperty("id_pedido", idPedido);

            return ResponseEntity.status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());

        } catch (IllegalArgumentException ex) {
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", ex.getMessage());
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        } catch (Exception ex) {
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", "Error interno");
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        }
    }
}