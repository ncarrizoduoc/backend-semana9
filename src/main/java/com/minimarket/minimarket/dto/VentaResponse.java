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

    public VentaResponse(Venta venta){
        id = venta.getId();
        usuario = new UsuarioResponse(venta.getUsuario());
        fecha = dateToString(venta.getFecha());
        detalles = new ArrayList<>();
        for (DetalleVenta detalle : venta.getDetalles()){
            detalles.add(new DetalleVentaResponse(detalle));
        }
    }

}
