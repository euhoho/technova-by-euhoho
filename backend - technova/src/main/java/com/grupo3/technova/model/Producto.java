package com.grupo3.technova.model;

import com.google.gson.JsonObject;
import java.math.BigDecimal;

public class Producto implements Jsonable {
    private Long id_producto;
    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;
    private String imagen;

    public Producto() {}

    public Producto(Long id_producto, String sku, String nombre, String descripcion,
                    BigDecimal precio, Integer stock, String categoria, String imagen) {
        this.id_producto = id_producto;
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    public Long getId_producto() { return id_producto; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public String getImagen() { return imagen; }

    @Override
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_producto", id_producto);
        json.addProperty("sku", sku);
        json.addProperty("nombre", nombre);
        json.addProperty("descripcion", descripcion);
        json.addProperty("precio", precio != null ? precio.toString() : null);
        json.addProperty("stock", stock);
        json.addProperty("categoria", categoria);
        json.addProperty("imagen", imagen);
        return json;
    }
}
