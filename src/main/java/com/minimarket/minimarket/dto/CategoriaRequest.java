package com.minimarket.minimarket.dto;

import java.util.ArrayList;

import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Producto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para ingresar objetos Categoria")
public class CategoriaRequest {
    @Schema(description = "ID de la categoria")
    @NotNull(message = "Debe ingresar un ID para la categoria")
    private Long id;
    
    @Schema(description = "nombre de la categoria")
    @NotNull(message = "Debe ingresar un nombre para la categoria")
    @NotBlank(message = "El nombre de la categoria no puede ser un texto en blanco")
    private String nombre;

    // Convertir objeto CategoriaRequest a Categoria
    public Categoria toCategoria(){
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setProductos(new ArrayList<Producto>());

        return categoria;
    }

}
