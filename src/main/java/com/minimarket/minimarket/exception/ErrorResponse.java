package com.minimarket.minimarket.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "DTO para respuestas de Internal Server Error")
public class ErrorResponse {
    @Schema(description = "Timestamp del momento donde se produjo el error", example = "2026-07-14T20:52:06.3772537")
    private LocalDateTime timestamp;

    @Schema(description = "Codigo de error", example = "500")
    private int status;

    @Schema(description = "Tipo de error", example = "Internal Server Error")
    private String error;

    @Schema(description = "Mensaje de error", example = "Error al agregar al carrito: No hay stock suficiente del producto: Arroz")
    private String message;

    @Schema(description = "Endpoint donde se produjo el error", example = "uri=/api/carrito")
    private String path;

}
