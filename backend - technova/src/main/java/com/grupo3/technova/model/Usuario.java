package com.grupo3.technova.model;

import com.google.gson.JsonObject;
import com.grupo3.technova.model.enums.EnumRol;

public class Usuario implements Jsonable {

    private Long id_usuario;
    private String email;
    private String nombre;
    private String password; // se guarda internamente pero NUNCA se envía al cliente
    private EnumRol rol;

    // Constructor vacío — Spring lo necesita en algunos contextos internos
    public Usuario() {}

    // Constructor completo — usado en el repositorio al leer la BD
    public Usuario(Long id_usuario, String email, String nombre, String password, EnumRol rol) {
        this.id_usuario = id_usuario;
        this.email = email;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    // Getters — para leer los atributos desde fuera de la clase
    public Long getId_usuario() { return id_usuario; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getPassword() { return password; }
    public EnumRol getRol() { return rol; }

    // Setters — para modificar los atributos desde fuera de la clase
    public void setId_usuario(Long id_usuario) { this.id_usuario = id_usuario; }
    public void setEmail(String email) { this.email = email; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(EnumRol rol) { this.rol = rol; }

    // Método de la interfaz Jsonable — convierte el objeto en un JsonObject de Gson
    @Override // indica que este método viene de la interfaz, no lo hemos inventado
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_usuario", id_usuario);
        json.addProperty("email", email);
        json.addProperty("nombre", nombre);
        // .name() convierte el enum a String — devuelve "CLIENTE", "OFICINA" o "ADMINISTRADOR"
        json.addProperty("rol", rol != null ? rol.name() : null);
        // IMPORTANTE: NO incluir password
        return json;
    }
}