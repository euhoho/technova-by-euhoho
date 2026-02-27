package com.grupo3.technova.controller;

import com.google.gson.JsonArray;
import com.grupo3.technova.model.Pedido;
import com.grupo3.technova.repository.PedidoRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

// @RestController le dice a Spring que esta clase atiende peticiones HTTP y que sus métodos devuelven datos directamente, no páginas HTML.
// @RequestMapping("/api") significa que todas las rutas de esta clase
@RestController
@RequestMapping("/api")
public class PedidoController {

    private final PedidoRepository repo;

    // Spring inyecta el repositorio automáticamente igual que con DataSource
    public PedidoController(PedidoRepository repo) {
        this.repo = repo;
    }

    // Atiende GET /api/pedidos — todos los parámetros son opcionales.
    // Ejemplos válidos:
    //   GET /api/pedidos
    //   GET /api/pedidos?estado=CONFIRMADO
    //   GET /api/pedidos?fechaIni=2026-01-01&fechaFin=2026-12-31
    //   GET /api/pedidos?estado=ENVIADO&fechaIni=2026-01-01&fechaFin=2026-12-31
    @GetMapping("/pedidos")
    public ResponseEntity<String> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaIni,
            @RequestParam(required = false) String fechaFin
    ) {
        // Las fechas llegan como String desde la URL ("2026-01-01")
        // y las convertimos a Date de SQL solo si no vienen vacías.
        // Date.valueOf() entiende el formato "yyyy-MM-dd" directamente.
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

    // Atiende POST /api/pedidos — crea un pedido completo.
    // JSON que espera recibir:
    // {
    //   "id_usuario": 7,
    //   "items": [
    //     { "id_producto": 1, "cantidad": 2 },
    //     { "id_producto": 5, "cantidad": 1 }
    //   ]
    // }
    @PostMapping("/pedidos")
    public ResponseEntity<String> crear(@RequestBody java.util.Map<String, Object> body) {
        try {
            // Extraemos id_usuario del body. viene como Number porque el JSON no distingue entre Integer y Long
            Number idUsuarioN = (Number) body.get("id_usuario");
            if (idUsuarioN == null) throw new IllegalArgumentException("Falta id_usuario");

            // @SuppressWarnings: para quitar avisos amarillos del ide
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> items =
                    (java.util.List<java.util.Map<String, Object>>) body.get("items");

            if (items == null || items.isEmpty()) throw new IllegalArgumentException("El pedido debe tener items");

            // Delegamos toda la lógica al repositorio. El repositorio se encargará de crear el pedido, crear las líneas de pedido y descontar el stock, todo dentro de una transacción.
            long idPedido = repo.crearPedidoYDescontarStock(idUsuarioN.longValue(), items);

            // Pedido creado correctamente — devolvemos el id generado
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "ok");
            json.addProperty("id_pedido", idPedido);

            return ResponseEntity.status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());

        } catch (IllegalArgumentException ex) {
            // Errores de validación que lanzamos nosotros mismos:
            // stock insuficiente, producto no existe, falta id_usuario, etc.
            // Devolvemos 400 Bad Request — el cliente envió datos incorrectos.
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", ex.getMessage());
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        } catch (Exception ex) {
                        // Cualquier otro error inesperado — devolvemos 500 Internal Server Error.
            // No enviamos el mensaje real del error al cliente por seguridad ya que podría revelar detalles internos del sistema.
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("status", "error");
            json.addProperty("message", "Error interno");
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.toString());
        }
    }
}