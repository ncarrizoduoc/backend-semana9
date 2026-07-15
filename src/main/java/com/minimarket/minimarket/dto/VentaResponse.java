package com.minimarket.minimarket.dto;

import java.util.ArrayList;
import java.util.List;

import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Venta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.minimarket.minimarket.config.FechaUtil.*;

@Data
@Schema(description = "DTO de respuesta para objetos Venta")
public class VentaResponse {

    @Schema(description = "ID de la venta", example = "10")
    private Long id;

    @Schema(description = "Usuario asociado a la venta")
    private UsuarioResponse usuario;

    @Schema(description = "Fecha de la venta", example = "2026-10-25 01:35:25")
    private String fecha;

    @Schema(description = "Detalles de venta asociados a la venta")
    private List<DetalleVentaResponse> detalles;

    @Schema(description = "Monto total de la venta", example = "29.980.0")
    private Double total;

    public VentaResponse(Venta venta){
        id = venta.getId();
        usuario = new UsuarioResponse(venta.getUsuario());
        fecha = dateToString(venta.getFecha());
        detalles = new ArrayList<>();
        total = 0.0;
        
        Double monto;
        for (DetalleVenta detalle : venta.getDetalles()){
            detalles.add(new DetalleVentaResponse(detalle));

            // Se suma el total parcial de cada producto al total de la venta
            monto = detalle.getPrecio() * detalle.getCantidad();
            total += monto;
        }
    }

}
