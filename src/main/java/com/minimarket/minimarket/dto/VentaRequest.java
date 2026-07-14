package com.minimarket.minimarket.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.minimarket.minimarket.config.FechaUtil.*;

@Data
@Schema(description = "DTO para ingresar objetos Venta")
public class VentaRequest {
    
    @Schema(description = "ID de la venta", example = "1")
    private Long id;
    
    @Schema(description = "ID del usuario asociado a la venta", example = "1")
    private Long usuarioId;
    
    @Schema(
        type = "string", 
        description = "Fecha de la venta",
        pattern = DATE_JSON_FORMAT,
        example = "2026-10-25 05:35:25")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_JSON_FORMAT)
    private Date fecha;

}
