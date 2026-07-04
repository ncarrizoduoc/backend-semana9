package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.Producto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para objetos Producto")
public class ProductoResponse {
    @Schema(description = "ID del producto")
    private Long id;
    @Schema(description = "Nombre del producto")
    private String nombre;
    @Schema(description = "Precio del producto")
    private Double precio;
    @Schema(description = "Stock disponible del producto")
    private Integer stock;
    @Schema(description = "Categoria del producto")
    private CategoriaResponse categoria;

    public ProductoResponse(Producto producto){
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.precio = producto.getPrecio();
        this.stock = producto.getStock();
        this.categoria = new CategoriaResponse(producto.getCategoria());
    }
}
