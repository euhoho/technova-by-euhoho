package com.grupo3.technova.dto;

import com.grupo3.technova.model.enums.EnumCategoria;
import java.math.BigDecimal;

// DTO para la petición de crear un producto (POST /api/productos).
// Al usar EnumCategoria en vez de String, el compilador valida que la categoría sea un valor válido.
public class ProductoRequest {

    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private EnumCategoria categoria;
    private String imagen;

    // Constructor vacío — necesario para que Spring pueda deserializar el JSON del @RequestBody
    public ProductoRequest() {}

    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public EnumCategoria getCategoria() { return categoria; }
    public String getImagen() { return imagen; }

    public void setSku(String sku) { this.sku = sku; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setCategoria(EnumCategoria categoria) { this.categoria = categoria; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}