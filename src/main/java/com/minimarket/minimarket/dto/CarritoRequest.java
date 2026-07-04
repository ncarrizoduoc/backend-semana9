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
    @NotNull(message = "Debe ingresar un ID para el carrito")
    private Long id;
    
    @NotNull(message = "Debe ingresar el ID del usuario")
    @PositiveOrZero(message = "El ID de usuario debe ser un numero mayor o igual a 0")
    private Long usuarioId;

    @NotNull(message = "Debe ingresar el ID del producto")
    @PositiveOrZero(message = "El ID de producto debe ser un numero mayor o igual a 0")
    private Long productoId;

    @NotNull(message = "Debe ingresar la cantidad del producto")
    @Positive(message = "La cantidad debe ser un numero positivo")
    private Integer cantidad;


}
