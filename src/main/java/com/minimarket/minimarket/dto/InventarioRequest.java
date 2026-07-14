package com.minimarket.minimarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

import static com.minimarket.minimarket.config.FechaUtil.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para ingresar objetos Inventario")
public class InventarioRequest {

    @Schema(description = "ID del movimiento de inventario", example = "0")
    private Long id;

    @Schema(description = "ID del producto asociado al movimiento de inenvtario", example = "1")
    @NotNull(message = "Debe introducir un producto")
    private Long productoId;
    
    @Schema(description = "Cantidad de producto que entra o sale", example = "5")
    @NotNull(message = "Debe introducir la cantidad")
    @Positive(message = "La cantidad debe ser un numero positivo")
    private Integer cantidad;

    @Schema(description = "Tipo de movimiento (Entrada o Salida)", example = "Entrada")
    @NotNull(message = "Debe introducir el tipo de movimiento")
    @Pattern(regexp = "Entrada|Salida", message = "El tipo de movimiento debe ser Entrada o Salida")
    private String tipoMovimiento; // Ejemplo: "Entrada" o "Salida"

    @Schema(
        type = "string", 
        description = "Fecha del movimiento de inventario",
        pattern = DATE_JSON_FORMAT,
        example = "2026-10-25 05:35:25")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_JSON_FORMAT)
    private Date fechaMovimiento;
    
}
