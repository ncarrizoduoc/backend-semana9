package com.minimarket.minimarket.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.minimarket.minimarket.entity.Inventario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.minimarket.minimarket.config.CONSTANTS.*;

@Data
@Schema(description = "DTO de respuesta para objetos Inventario")
public class InventarioResponse {
    @Schema(description = "ID del movimiento de inventario")
    private Long id;
    @Schema(description = "Producto asociado al movimiento de inventario")
    private ProductoResponse producto;
    @Schema(description = "Cantidad de producto que entra o sale")
    private Integer cantidad;
    @Schema(description = "Tipo de movimiento de inventario")
    private String tipoMovimiento;
    @Schema(description = "Fecha del movimiento de inventario")
    private String fechaMovimiento;

    public InventarioResponse(Inventario inventario){
        id = inventario.getId();
        // Se usa el DTO ProductoResponse en vez de Producto
        producto = new ProductoResponse(inventario.getProducto());
        cantidad = inventario.getCantidad();
        tipoMovimiento = inventario.getTipoMovimiento();
        
        // Convertir fecha a String con formato
        Date fechaOriginal = inventario.getFechaMovimiento();
        var localDateTime = fechaOriginal.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_JSON_FORMAT);
        String fechaFormateada = localDateTime.format(formatter);
        fechaMovimiento = fechaFormateada;
    }

}
