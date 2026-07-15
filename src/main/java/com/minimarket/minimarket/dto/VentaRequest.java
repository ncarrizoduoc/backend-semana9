package com.minimarket.minimarket.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.minimarket.minimarket.config.FechaUtil.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO para ingresar objetos Venta")
public class VentaRequest {
    
    @Schema(description = "ID de la venta", example = "1")
    private Long id;
    
    @Schema(description = "ID del usuario asociado a la venta", example = "1")
    @NotNull(message = "Debe ingresar un ID de usuario")
    private Long usuarioId;
    
    @Schema(
        type = "string", 
        description = "Fecha de la venta",
        pattern = DATE_JSON_FORMAT,
        example = "2026-10-25 05:35:25")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_JSON_FORMAT)
    @NotNull(message = "Debe ingresar la fecha de la venta")
    private Date fecha;

}
