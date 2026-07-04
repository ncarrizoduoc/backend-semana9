package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.Carrito;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para objetos Carrito")
public class CarritoResponse {
    @Schema(description = "ID del carrito")
    private Long id;

    @Schema(description = "Usuario asociado al carrito")
    private UsuarioResponse usuario;

    @Schema(description = "Producto incluido en el carrito")
    private ProductoResponse producto;

    @Schema(description = "Unidades del producto en el carrito")
    private Integer cantidad;

    public CarritoResponse(Carrito carrito){
        id = carrito.getId();
        usuario = new UsuarioResponse(carrito.getUsuario());
        producto = new ProductoResponse(carrito.getProducto());
        cantidad = carrito.getCantidad();
    }

}
