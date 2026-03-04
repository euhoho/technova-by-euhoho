package com.grupo3.technova.dto;

import java.util.List;
import java.util.Map;

// DTO para la petición de crear un pedido (POST /api/pedidos).
// Encapsula los datos que llegan del cliente en vez de usar Map<String, Object>.
public class PedidoRequest {

    private Long id_usuario;
    private List<Map<String, Object>> items;

    // Constructor vacío — necesario para que Spring pueda deserializar el JSON del @RequestBody
    public PedidoRequest() {}

    public Long getId_usuario() { return id_usuario; }
    public List<Map<String, Object>> getItems() { return items; }

    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}