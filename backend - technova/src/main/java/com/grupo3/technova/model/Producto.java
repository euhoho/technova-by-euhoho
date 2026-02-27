package com.grupo3.technova.model;

import com.google.gson.JsonObject;
import java.math.BigDecimal;

public class Producto implements Jsonable {
        // Son privados para que nadie los modifique directamente desde fuera de la clase.
    private Long id_producto;
    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio; // BigDecimal en vez de double para evitar errores de redondeo
    private Integer stock;
    private String categoria;
    private String imagen;

    // Constructor vacío — Spring lo necesita en algunos contextos internos
    public Producto() {}

    // Constructor completo — lo usamos en el repositorio cuando leemos una fila de la DB y queremos crear un objeto Producto con sus datos
    public Producto(Long id_producto, String sku, String nombre, String descripcion, BigDecimal precio, Integer stock, String categoria, String imagen) {
        this.id_producto = id_producto;
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    // Getters — para leer los atributos desde fuera de la clase
    public Long getId_producto() { return id_producto; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public String getImagen() { return imagen; }

    // Método de la interfaz Jsonable — convierte el objeto en un JsonObject de Gson para poder enviarlo como respuesta JSON al cliente
    @Override // indica que este método viene de la interfaz, no lo hemos inventado
    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("id_producto", id_producto);
        json.addProperty("sku", sku);
        json.addProperty("nombre", nombre);
        json.addProperty("descripcion", descripcion);
        // precio.toString() evita notación científica en el JSON.
        // El null check es por si precio fuese null, para no petar.
        json.addProperty("precio", precio != null ? precio.toString() : null);
        json.addProperty("stock", stock);
        json.addProperty("categoria", categoria);
        json.addProperty("imagen", imagen);
        return json;
    }   
}
