package com.minimarket.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO para ingresar objetos DetalleVenta")
public class DetalleVentaRequest {

    @Schema(description = "ID del detalle de venta", example = "1")
    private Long id;
    
    @Schema(description = "ID de la venta a la que corresponde el detalle de venta", example = "1")
    @NotNull(message = "Debe ingresar un ID de venta")
    private Long ventaId;

    @Schema(description = "ID del producto asociado al detalle de venta", example = "1")
    @NotNull(message = "Debe ingresar un ID de producto")
    private Long productoId;

    @Schema(description = "Cantidad del producto", example = "5")
    @NotNull(message = "Debe ingresar la cantidad de producto")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Schema(description = "Precio de venta del producto", example = "1990.0")
    @NotNull(message = "Debe ingresar un precio")
    @PositiveOrZero(message = "El precio debe ser mayor o igual a 0") // Se admite 0 porque puede venir de regalo por promocion
    private Double precio;

}
