package com.minimarket.minimarket.openapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Formato de error para Bad Request")
public class BadRequestResponse {
    @Schema(description = "Mensaje explicando la causa del error", example = "Failed to read request")
    private String detail;
    @Schema(description = "Endpoint que generó el error", example = "/auth/login")
    private String instance;
    @Schema(description = "Código de status HTTP", example = "400")
    private int status;
    @Schema(description = "Nombre del status HTTP", example = "Bad Request")
    private String title;

}
