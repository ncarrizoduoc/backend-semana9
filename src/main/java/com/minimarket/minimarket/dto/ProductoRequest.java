package com.minimarket.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para ingresar objetos Producto")
public class ProductoRequest {

    @Schema(description = "ID del producto", example = "0")
    private Long id;
    
    @Schema(description = "Nombre del producto", example = "Leche")
    @NotNull(message = "Debe ingresar un nombre para el producto")
    @NotBlank(message = "El nombre del producto no puede ser un texto en blanco")
    private String nombre;

    @Schema(description = "Precio del producto", example = "1990.0")
    @NotNull(message = "Debe ingresar el precio del producto")
    @Positive(message = "El precio del producto debe ser un numero positivo")
    private Double precio;
    
    @Schema(description = "Stock disponible del producto", example = "100")
    @PositiveOrZero(message = "El stock del producto debe ser mayor o igual a 0")
    @NotNull(message = "Debe ingresar el stock del producto")
    private Integer stock;
    
    @Schema(description = "ID de la categoria del producto", example = "1")
    @NotNull(message = "Debe ingresar una categoria")
    @PositiveOrZero(message = "El ID de categoria debe ser igual o mayor a 0")
    private Long categoriaId;

}
