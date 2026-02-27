package com.grupo3.technova.model;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.Timestamp; // tipo de Java para fechas que vienen de MySQL con hora incluida

public class Pedido implements Jsonable {
    private Long id_pedido;
    private Timestamp fecha;
    private BigDecimal total_pedido;
    private String pedido_estado;

    private Long id_usuario;
    private String email;
    private String rol;

    public Pedido(Long id_pedido, Timestamp fecha, BigDecimal total_pedido, String pedido_estado,
                  Long id_usuario, String email, String rol) {
        this.id_pedido = id_pedido;
        this.fecha = fecha;
        this.total_pedido = total_pedido;
        this.pedido_estado = pedido_estado;
        this.id_usuario = id_usuario;
        this.email = email;
        this.rol = rol;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_pedido", id_pedido);
        json.addProperty("fecha", fecha != null ? fecha.toString() : null);
        json.addProperty("total_pedido", total_pedido != null ? total_pedido.toString() : "0.00");
        json.addProperty("pedido_estado", pedido_estado);

        JsonObject usuario = new JsonObject();
        usuario.addProperty("id_usuario", id_usuario);
        usuario.addProperty("email", email);
        usuario.addProperty("rol", rol);

        json.add("usuario", usuario);
        return json;
    }
}
