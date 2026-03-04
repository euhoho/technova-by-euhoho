package com.grupo3.technova.model;
import com.grupo3.technova.model.enums.EnumRol;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.Timestamp; // tipo de Java para fechas que vienen de MySQL con hora incluida

public class Pedido implements Jsonable {
    // Son privados para que nadie los modifique directamente desde fuera de la clase.
    private Long id_pedido;
    private Timestamp fecha; // Timestamp incluye fecha Y hora, no solo fecha
    private BigDecimal total_pedido;
    private String pedido_estado;

     // Datos del usuario asociado — vienen del JOIN con la tabla usuario. Solo los campos que necesitamos mostrar
    private Long id_usuario;
    private String email;
    private EnumRol rol;

    // Solo tiene constructor completo — un pedido siempre necesita todos sus datos
    public Pedido(Long id_pedido, Timestamp fecha, BigDecimal total_pedido, String pedido_estado, Long id_usuario, String email, EnumRol rol) {
        this.id_pedido = id_pedido;
        this.fecha = fecha;
        this.total_pedido = total_pedido;
        this.pedido_estado = pedido_estado;
        this.id_usuario = id_usuario;
        this.email = email;
        this.rol = rol;
    }

    @Override // indica que este método viene de la interfaz, no lo hemos inventado
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_pedido", id_pedido);
        // fecha se convierte a String en formato "2026-02-17 10:30:00.0"
        json.addProperty("fecha", fecha != null ? fecha.toString() : null);
        // si total_pedido fuese null devolvemos "0.00" en vez de null
        json.addProperty("total_pedido", total_pedido != null ? total_pedido.toString() : "0.00");
        json.addProperty("pedido_estado", pedido_estado);

        // Los datos del usuario se meten dentro de un objeto anidado. Así sabe que el email es del usuario, no del pedido.
        JsonObject usuario = new JsonObject();
        usuario.addProperty("id_usuario", id_usuario);
        usuario.addProperty("email", email);
        usuario.addProperty("rol", rol != null ? rol.name() : null);
        // json.add en vez de json.addProperty porque estamos metiendo un objeto dentro de otro objeto, no un valor simple
        json.add("usuario", usuario);
        return json;
    }
}
