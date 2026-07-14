package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.DetalleVenta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para objetos DetalleVenta")
public class DetalleVentaResponse {

    @Schema(description = "ID del detalle de venta", example = "2")
    private Long id;
    @Schema(description = "ID de la venta a la que corresponde el detalle", example = "10")
    private Long ventaId;
    @Schema(description = "Producto asociado al detalle de venta")
    private ProductoResponse producto;
    @Schema(description = "Cantidad de producto", example = "12")
    private Integer cantidad;
    @Schema(description = "Precio de venta del producto", example = "1590.0")
    private Double precio;

    public DetalleVentaResponse(DetalleVenta detalle){
        id = detalle.getId();
        ventaId = detalle.getVenta().getId();
        producto = new ProductoResponse(detalle.getProducto());
        cantidad = detalle.getCantidad();
        precio = detalle.getPrecio();


    }

}
