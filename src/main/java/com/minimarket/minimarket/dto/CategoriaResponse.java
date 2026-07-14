package com.minimarket.minimarket.dto;

import com.minimarket.minimarket.entity.Categoria;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta para objetos Categoria")
public class CategoriaResponse {
    @Schema(description = "ID de la categoria", example = "3")
    private Long id;
    
    @Schema(description = "Nombre de la categoria", example = "Bebestibles")
    private String nombre;

    public CategoriaResponse(Categoria categoria){
        this.id = categoria.getId();
        this.nombre = categoria.getNombre();
    }

}
