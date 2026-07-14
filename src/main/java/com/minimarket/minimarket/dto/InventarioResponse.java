package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.Inventario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.minimarket.minimarket.config.FechaUtil.*;

@Data
@Schema(description = "DTO de respuesta para objetos Inventario")
public class InventarioResponse {
    @Schema(description = "ID del movimiento de inventario", example = "5")
    private Long id;
    @Schema(description = "Producto asociado al movimiento de inventario")
    private ProductoResponse producto;
    @Schema(description = "Cantidad de producto que entra o sale", example = "14")
    private Integer cantidad;
    @Schema(description = "Tipo de movimiento de inventario", example = "Entrada")
    private String tipoMovimiento;
    @Schema(description = "Fecha del movimiento de inventario", example = "2026-10-25 01:35:25")
    private String fechaMovimiento;

    public InventarioResponse(Inventario inventario){
        id = inventario.getId();
        // Se usa el DTO ProductoResponse en vez de Producto
        producto = new ProductoResponse(inventario.getProducto());
        cantidad = inventario.getCantidad();
        tipoMovimiento = inventario.getTipoMovimiento();
        fechaMovimiento = dateToString(inventario.getFechaMovimiento());
    }

}
