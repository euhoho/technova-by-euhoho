package com.grupo3.technova.model;

import com.google.gson.JsonObject;

// Interfaz que obliga a todas las clases que la implementen a tener un método toJsonObject().
public interface Jsonable {
    JsonObject toJsonObject();
}