package com.grupo3.technova.model;

import com.google.gson.JsonObject;

public class Usuario implements Jsonable {
    private Long id_usuario;
    private String email;
    private String password;
    private String rol;

    public Usuario() {}

    public Usuario(Long id_usuario, String email, String password, String rol) {
        this.id_usuario = id_usuario;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public Long getId_usuario() { return id_usuario; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }

    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
public JsonObject toJsonObject() {
    JsonObject json = new JsonObject();
    json.addProperty("id_usuario", id_usuario);
    json.addProperty("email", email);
    json.addProperty("rol", rol);
    // IMPORTANTE: NO incluir password
    return json;
}
}
