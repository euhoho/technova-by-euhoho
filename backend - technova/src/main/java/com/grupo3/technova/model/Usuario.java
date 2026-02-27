package com.grupo3.technova.model;

import com.google.gson.JsonObject;

public class Usuario implements Jsonable {
    private Long id_usuario;
    private String email;
    private String password; // se guarda internamente pero NUNCA se envía al cliente
    private String rol;

    // Constructor vacío — Spring lo necesita en algunos contextos internos
    public Usuario() {}

    // Constructor completo — usado en el repositorio al leer la DB
    public Usuario(Long id_usuario, String email, String password, String rol) {
        this.id_usuario = id_usuario;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Getters — para leer los atributos desde fuera de la clase
    public Long getId_usuario() { return id_usuario; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }

    // Setters — para modificar los atributos desde fuera de la clase (cambiar email, rol, etc.)
    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol) { this.rol = rol; }

    // Método de la interfaz Jsonable — convierte el objeto en un JsonObject de Gson para poder enviarlo como respuesta JSON al cliente
    @Override // indica que este método viene de la interfaz, no lo hemos inventado
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_usuario", id_usuario);
        json.addProperty("email", email);
        json.addProperty("rol", rol);
        // IMPORTANTE: NO incluir password
        return json;
    }
}
