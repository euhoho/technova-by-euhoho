package com.grupo3.technova.dto;

// DTO (Data Transfer Object) para la petición de login.
// Encapsula los datos que llegan del cliente en POST /api/login.
// Ventaja frente a Map<String, String>: los campos están tipados.
// Si falta "email" o "password" en el JSON, lo detectamos en cuanto intentamos usarlo.
public class LoginRequest {

    private String email;
    private String password;

    // Constructor vacío — necesario para que Spring pueda deserializar el JSON del @RequestBody
    public LoginRequest() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}