package com.minimarket.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para ingresar objetos Carrito")
public class CarritoRequest {
    
    @Schema(description = "ID del carrito", example = "0")
    private Long id;
    
    @Schema(description = "ID del usuario asociado al carrito", example = "1")
    @NotNull(message = "Debe ingresar el ID del usuario")
    @PositiveOrZero(message = "El ID de usuario debe ser un numero mayor o igual a 0")
    private Long usuarioId;

    @Schema(description = "ID del producto agregado al carrito", example = "1")
    @NotNull(message = "Debe ingresar el ID del producto")
    @PositiveOrZero(message = "El ID de producto debe ser un numero mayor o igual a 0")
    private Long productoId;

    @Schema(description = "Cantidad de producto en el carrito", example = "5")
    @NotNull(message = "Debe ingresar la cantidad del producto")
    @Positive(message = "La cantidad debe ser un numero positivo")
    private Integer cantidad;


}
